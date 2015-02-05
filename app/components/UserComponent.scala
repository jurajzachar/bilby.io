package components

import scala.language.implicitConversions
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.StaticQuery.staticQueryToInvoker

import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

import models.User
import models.UserProfile
import models.Visitor
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session

trait UserComponent {

  def InitUserComponent(cake: ActiveSlickCake) = new UserComponent(cake)

  class UserComponent(val cake: ActiveSlickCake) {

    import cake._

    val log = LoggerFactory.getLogger(this.getClass)
    
    type Candidate = ((Visitor, UserProfile), User)
    
    def getIdByUserName(userName: String): Option[User#Id] = {
      DB.withSession {
        implicit session: Session =>
          val idByUserName = sql"""select ID from "user" where USER_NAME = $userName""".as[Long]
          idByUserName.list.headOption
      }
    }

    def handleVisitor(visitor: Visitor): Option[Visitor#Id] = {
      Visitors.extractId(visitor) match {
        case None => {
          DB.withSession {
            implicit session: Session =>
              (visitor.save).id
          }
        }
        case Some(id) => Option(id)
      }
    }

    def handleUserProfile(userProfile: UserProfile): Option[UserProfile#Id] = {
      UserProfiles.extractId(userProfile) match {
        case None => {
          DB.withSession {
            implicit session: Session =>
              (userProfile.save).id
          }
        }
        case Some(id) => Option(id) //profile exists
      }
    }

    def signUpNewUser(uw: User#WrappedUser): Option[User] = {
      getIdByUserName(uw._2.userName) match {
        case Some(id) =>
          println(s"${uw._2.userName} is taken!"); None //username taken
        case None => {
          //unique username registers...  
          DB.withSession {
            implicit session: Session =>
              Option {
                User(
                  uw._2.firstName,
                  uw._2.lastName,
                  uw._2.userName.toLowerCase,
                  uw._2.email.toLowerCase,
                  uw._2.password,
                  uw._2.avatarUrl,
                  uw._2.authMethod,
                  uw._2.oAuth1Info,
                  uw._2.oAuth2Info,
                  uw._2.passwordInfo,
                  { if (uw._1._2 == null) None else handleUserProfile(uw._1._2) },
                  { if (uw._1._1 == null) None else handleVisitor(uw._1._1) } //visitor_id
                  ).save
              }
          }
        }
      }
    }

    def authenticate(userName: String, password: String): Option[User] = {
      getIdByUserName(userName) match {
        case Some(id) => {
          DB.withSession {
            implicit session: Session =>
              val candidate = Users.findById(id)
              if (BCrypt.checkpw(password, candidate.password)) Some(candidate)
              else None
          }
        }
        case _ => None
      }
    }
  }
}