package components

import scala.Left
import scala.Right
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

  val emailRe = """(\w+)@([\w\.]+)""".r

  def initComponent(cake: ActiveSlickCake = ActiveSlickCake.cake) = new UserComponent(cake)

  lazy val dal = initComponent()

  /**
   * @author juri
   * Data access layer functions for User
   */
  class UserComponent(val cake: ActiveSlickCake) {

    import cake._

    val log = LoggerFactory.getLogger(this.getClass)
    val emailTaken = "This email address (%s) address is already taken"
    val usernameTaken = "This username (%s) is already taken"
    val invalidLogin = "Wrong combination of username/email address and password"

    type RejectReason = (String, String) //format, arg
    type SignUpResult = Either[User, RejectReason]

    /**
     * @param key
     * @return
     */
    def getIdByEmail(key: String): Option[User#Id] = {
      emailRe findFirstIn key match {
        case Some(email) => {
          DB.withSession {
            implicit session: Session =>
              sql"""select ID from "user" where EMAIL = $key""".as[Long].list.headOption
          }
        }
        case None => None
      }
    }

    /**
     * @param key
     * @return
     */
    def getIdByUsername(key: String): Option[User#Id] = {
      DB.withSession {
        implicit session: Session =>
          sql"""select ID from "user" where USER_NAME = $key""".as[Long].list.headOption
      }
    }

    /**
     * @param key
     * @return
     */
    def getIdByUniqueKey(key: String): Option[User#Id] = {
      (getIdByEmail(key), getIdByUsername(key)) match {
        case (Some(id), None) => Some(id)
        case (None, Some(id)) => Some(id)
        case (_, _)           => None
      }
    }

    /**
     * @param visitor
     * @return
     */
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

    /**
     * @param userProfile
     * @return
     */
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

    /**
     * @param wrapped
     * @return
     */
    def signUpNewUser(wrapped: User#Wrapped): SignUpResult = {
      (getIdByUsername(wrapped._3.username), getIdByUsername(wrapped._3.email)) match {
        case (Some(id), _) =>
          Right(usernameTaken, wrapped._3.username) //username taken
        case (_, Some(id)) => Right(emailTaken, wrapped._3.email) //username taken
        case (None, None) => {
          //unique username registers...  
          DB.withSession {
            implicit session: Session =>
              Left(
                User(
                  wrapped._3.firstName,
                  wrapped._3.lastName,
                  wrapped._3.username.toLowerCase,
                  wrapped._3.email.toLowerCase,
                  wrapped._3.password,
                  wrapped._3.avatarUrl,
                  wrapped._3.authMethod,
                  wrapped._3.oAuth1Info,
                  wrapped._3.oAuth2Info,
                  wrapped._3.passwordInfo,
                  { if (wrapped._2 == null) None else handleUserProfile(wrapped._2) },
                  { if (wrapped._1 == null) None else handleVisitor(wrapped._1) } //visitor_id
                  ).save)
          }
        }
      }
    }

    /**
     * Authentication call before session is built.
     * @param key
     * @param password
     * @return
     */
    def authenticate(key: String, password: String): Option[User] = {
      getIdByUniqueKey(key) match {
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

    /**
     * Internal call when username is known and call is Secured.
     * @param key
     * @return
     */
    /**
     * @param key
     * @return
     */
    def findUserById(key: String): Option[User] = {
      getIdByUniqueKey(key) match {
        case Some(id) => {
          DB.withSession {
            implicit session: Session =>
              Some(Users.findById(id))
          }
        }
        case _ => None
      }
    }

    /**
     * @param user
     * @return
     */
    def findUserProfile(user: User): Option[UserProfile] = {
      user.userprofile_id match {
        case Some(id) => {
          DB.withSession {
            implicit session: Session =>
              Some(UserProfiles.findById(id))
          }
        }
        case _ => None
      }
    }

    /**
     * @param user
     * @return
     */
    def findVisitor(user: User): Option[Visitor] = {
      user.userprofile_id match {
        case Some(id) => {
          DB.withSession {
            implicit session: Session =>
              Some(Visitors.findById(id))
          }
        }
        case _ => None
      }
    }

    def updateVisitor(userId: String, visitor: Visitor) {
      DB.withSession {
        implicit session: Session =>
          findUserById(userId) match {
            case Some(user) => {
              if (user.visitor_id.isDefined) Visitors.update(Visitor(visitor.host, visitor.timestamp, user.visitor_id))
              else {
                val visitor_id = Visitors.save(visitor).id
                DB.withSession {
                  implicit session: Session =>
                    sqlu"""update "user" SET visitor_id = $visitor_id WHERE id=${user.id.get}""".first
                }
              }
            }
            case None => //eeek!
          }
      }
    }
  }
}