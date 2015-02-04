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

    def signUpNewUser(user: User, userProfile: UserProfile, visitor: Visitor): Option[User] = {
      getIdByUserName(user.userName) match {
        case Some(id) =>
          println(s"${user.userName} is taken!"); None //username taken
        case None => {
          //unique username registers...  
          DB.withSession {
            implicit session: Session =>
              Option {
                User(
                  user.firstName,
                  user.lastName,
                  user.userName.toLowerCase,
                  user.email.toLowerCase,
                  user.password,
                  user.avatarUrl,
                  user.authMethod,
                  user.oAuth1Info,
                  user.oAuth2Info,
                  user.passwordInfo,
                  { if (userProfile == null) None else handleUserProfile(userProfile) },
                  { if (visitor == null) None else handleVisitor(visitor) } //visitor_id
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