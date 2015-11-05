package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.Tables
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.blueskiron.bilby.io.db.Tables.{ UserRow, UserprofileRow, VisitorRow }
import scala.concurrent.Promise
import com.blueskiron.bilby.io.model.{ Visitor, UserProfile, User }
import com.blueskiron.bilby.io.db.ar.ModelImplicits._
import com.blueskiron.bilby.io.db.ActiveSlickRepos.{ UserRepo, VisitorRepo, UserprofileRepo }

/**
 * UserDao trait uses cake pattern to inject desired {@link ApplicationDatabase}
 * and performs additional operations to CRUD provided by ActiveSlick.
 * @author juri
 *
 */
trait UserDao {

  sealed trait SignupRejection
  case class UserNameAlreadyTaken(userName: String) extends SignupRejection
  case class EmailAddressAleadyRegistered(email: String) extends SignupRejection

  type SignupOutcome = Either[UserRow, SignupRejection]

  /**
   * Initialize this dao trait with a specific instance of ApplicationDatabase.
   * (defaults to PostgresDatabase)
   * @param cake
   * @return
   */
  def initWithApplicationDatabase(cake: ApplicationDatabase = PostgresDatabase) = new UserDao(cake)

  /**
   * @return initialized UserDao with a default ApplicationDatabase
   */
  lazy val userDao = initWithApplicationDatabase()

  class UserDao(val cake: ApplicationDatabase) {

    import cake.jdbcProfile.api._

    private val emailRe = """(\w+)@([\w\.]+)""".r
    val log = LoggerFactory.getLogger(this.getClass)
    val emailTaken = "This email address (%s) address is already taken"
    val usernameTaken = "This username (%s) is already taken"
    val invalidLogin = "Wrong combination of username/email address and password"

    /* RAW queries */
    private def userFromEmailQuery(email: Rep[String]) = {
      for (u <- Tables.User if u.email === email) yield u
    }

    private def userFromUserNameQuery(email: Rep[String]) = {
      for (u <- Tables.User if u.userName === email) yield u
    }

    private def visitorFromHostQuery(host: Rep[String]) = {
      for (v <- Tables.Visitor if v.host === host) yield v
    }

    private def userProfileFromAllQuery(country: Rep[Option[String]], placeOfRes: Rep[Option[String]], age: Rep[Option[Short]]) = {
      for (up <- Tables.Userprofile if up.country === country && up.placeOfRes === placeOfRes && up.age === age)
        yield up
    }

    /* COMPILED queries */
    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val userFromEmail = Compiled(userFromEmailQuery _)

    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val userFromUserName = Compiled(userFromUserNameQuery _)

    /**
     * Compiled query for getting {@link VisitorRow} by host
     */
    val visitorFromHost = Compiled(visitorFromHostQuery _)

    /**
     * Compiled query for getting {@link UserprofileRow} by all its fields except of id
     */
    val userProfileFromAll = Compiled(userProfileFromAllQuery _)

    /* DAO functions */

    /**
     * @param user
     * @return
     */
    def signupUser(user: User): Future[SignupOutcome] = {
      val p = Promise[SignupOutcome]()
      //check for right-hand side: SignupRejection
      val userNameQ = userFromUserName(user.userName).result.headOption
      val emailQ = userFromEmail(user.email).result.headOption
      val outerAggregate = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield (x, y)
      outerAggregate.map {
        //invalid registration --> complete with value
        case (Some(x), _) => p.success(Right(UserNameAlreadyTaken(x.userName)))
        case (_, Some(x)) => p.success(Right(EmailAddressAleadyRegistered(x.email)))
        //valid new user --> complete with this future instead
        case _    => p.completeWith( foldNewUser(user) map { Left(_) })
      }
      p.future
    }

    /**
     * Save user and all optional sign up information
     * @param user
     * @return
     */
    private def foldNewUser(user: User): Future[UserRow] = {
      val extras = (
        user.visitor map { v => handleVisitor(v) },
        user.userprofile map { up => handleUserProfile(up) })

      extras match {

        //both visitor and user profile are handled
        case (Some(vF), Some(upF)) => {
          val aggregate = for {
            vis <- vF
            up <- upF
          } yield (vis, up)
          aggregate flatMap {
            case (v: Visitor, up: UserProfile) =>
              cake.commit(UserRepo.save(userRowFromUser(User.userWithProfileAndVisitor(user, Some(up), Some(v)))))
          }
        }
        //only visitor is handled
        case (Some(v), None) => {
          v flatMap { visitor =>
            cake.commit(UserRepo.save(userRowFromUser(User.userWithProfileAndVisitor(user, None, Some(visitor)))))
          }
        }

        //neither is handled
        case _ => cake.commit(UserRepo.save(userRowFromUser(User.userWithProfileAndVisitor(user, None, None))))
      }

    }

    def visitorFromOption(id: Option[Long]) = {
      id map { x => cake.runAction(VisitorRepo.findById(x)) }
    }

    def userProfileFromOption(id: Option[Long]) = {
      id map { x => cake.runAction(UserprofileRepo.findById(x)) }
    }

    def userFromUserRow(userRow: UserRow) = {
      (visitorFromOption(userRow.visitorId),
        userProfileFromOption(userRow.userprofileId)) match {

          case (Some(visitorF), Some(userProfileF)) => {
            val aggregate = for {
              vr <- visitorF
              upr <- userProfileF
            } yield (vr, upr)
            aggregate.map {
              case (v: VisitorRow, upr: UserprofileRow) => userFromRows(userRow, Some(upr), Some(v))
            }
          }
          case (Some(visitorF), None) => visitorF.map { v => userFromRows(userRow, None, Some(v)) }
          case (None, Some(userProfileF)) => userProfileF.map { upr => userFromRows(userRow, Some(upr), None) }
          case _ => Future { userFromRows(userRow, None, None) }
        }
    }

    /**
     * Create a future of optional user based on provided user name or email address.
     * @param key
     * @return
     */
    def userFromEitherUserNameOrEmail(key: String): Future[Option[User]] = {
      val userNameQ = userFromUserName(key).result.headOption
      val emailQ = userFromEmail(key).result.headOption
      val p = Promise[Option[User]]()
      val aggregateFuture = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield (x, y)
      aggregateFuture.map {
        case (Some(x), None) => p.success(Some(userFromRows(x, None, None)))
        case (None, Some(x)) => p.success(Some(userFromRows(x, None, None)))
        case _               => p.success(None)
      }
      p.future
    }

    /**
     * Create a future of {@VisitorRow}. If visitor host exists then update its timestamp.
     * @param visitor
     * @return
     */
    def handleVisitor(visitor: Visitor): Future[Visitor] = {
      val visitorF = cake.runAction {
        visitorFromHost(visitor.host).result.headOption
      }
      visitorF.flatMap {
        case Some(visitor) => cake.commit(VisitorRepo.save(visitor.copy(timestamp = System.currentTimeMillis())))
        case None          => cake.commit(VisitorRepo.save(visitor))
      } map { visitorFromVisitorRow(_) }
    }

    /**
     * Create a future of {@link UserProfileRow}. If UserProfile exists, return it, if not then save it.
     * @param userProfile
     * @return
     */
    def handleUserProfile(userProfile: UserProfile): Future[UserProfile] = {
      val userProfileF = cake.runAction {
        val tuples = UserProfile.unapply(userProfile).get
        userProfileFromAll(tuples._1, tuples._2, tuples._3).result.headOption
      }
      userProfileF.flatMap {
        case Some(up) => Future(up)
        case None     => cake.commit(UserprofileRepo.save(userProfile))
      } map { userprofileFromUserprofileRow(_) }
    }

  }
}