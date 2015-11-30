package com.blueskiron.bilby.io.db.dao

import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Promise, Future }
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.Tables
import com.blueskiron.bilby.io.db.Tables.{ AccountRow, UserRow, UserprofileRow, VisitorRow }
import com.blueskiron.bilby.io.db.ar.ModelImplicits
import com.blueskiron.bilby.io.api.model.{ Account, Visitor, UserProfile, User }
import com.blueskiron.bilby.io.db.ActiveSlickRepos.{ AccountRepo, UserRepo, VisitorRepo, UserprofileRepo }
import com.blueskiron.bilby.io.api.UserService.{ SignupOutcome, EmailAddressAleadyRegistered, UserNameAlreadyTaken }
import com.blueskiron.bilby.io.api.UserService.UnexpectedSignupError
import com.blueskiron.bilby.io.api.UserService.SignupOutcome

/**
 * UserDao trait uses cake pattern to inject desired {@link ApplicationDatabase}
 * and performs additional operations to CRUD provided by ActiveSlick.
 * @author juri
 *
 */
trait UserDao {

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

  protected class UserDao(val cake: ApplicationDatabase) {

    import cake.jdbcProfile.api._

    private val emailRe = """(\w+)@([\w\.]+)""".r
    val log = LoggerFactory.getLogger(this.getClass)
    val emailTaken = "This email address (%s) address is already taken"
    val usernameTaken = "This username (%s) is already taken"
    val invalidLogin = "Wrong combination of username/email address and password"

    /* RAW queries */
    private def userFromEmailQuery(email: Rep[String]) = {
      for {
        (u, a) <- Tables.User join Tables.Account on (_.accountId === _.id) if (a.email === email)
      } yield u
    }

    private def userFromUserNameQuery(userName: Rep[String]) = {
      for {
        (u, a) <- Tables.User join Tables.Account on (_.accountId === _.id) if (u.userName === userName)
      } yield u
    }

    private def visitorFromHostQuery(host: Rep[String]) = {
      for (v <- Tables.Visitor if v.host === host) yield v
    }

    private def fullUserFromIdQuery(id: Rep[Long]) = {
      for {
        (((u, a), up), vis) <- Tables.User join Tables.Account on (_.accountId === _.id) joinLeft Tables.Userprofile on (_._1.userprofileId === _.id) joinLeft Tables.Visitor on (_._1._1.visitorId === _.id)
        if u.id === id
      } yield (u, a, up, vis)
    }

    private def userProfileFromAllQuery(
        firstName: Rep[Option[String]],
        lastName: Rep[Option[String]],
        country: Rep[Option[String]], 
        placeOfRes: Rep[Option[String]], 
        age: Rep[Option[Short]]) = {
      for (up <- Tables.Userprofile 
          if up.firstName === firstName && up.lastName === lastName && up.country === country && up.placeOfRes === placeOfRes && up.age === age)
        yield up
    }

    /* COMPILED queries */

    /**
     * Compiled query for getting {@link UserRow} by userId
     */
    val compiledFullUserFromId = Compiled(fullUserFromIdQuery _)
    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val compiledUserFromEmail = Compiled(userFromEmailQuery _)

    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val compiledUserFromUserName = Compiled(userFromUserNameQuery _)

    /**
     * Compiled query for getting {@link VisitorRow} by host
     */
    val compiledVisitorFromHost = Compiled(visitorFromHostQuery _)

    /**
     * Compiled query for getting {@link UserprofileRow} by all its fields except of id
     */
    val compiledUserProfileFromAllParams = Compiled(userProfileFromAllQuery _)

    /* DAO functions */
    def deactivateUser(user: User): Future[User] = {
      ???  
    }
    
    def purgeFullUser(user: User): Future[Unit] = {
      ???  
    }
    
    /**
     * @param user
     * @return
     */
    def signupUser(user: User): Future[SignupOutcome] = {
      val p = Promise[SignupOutcome]()
      //check for right-hand side: SignupRejection
      val userNameQ = compiledUserFromUserName(user.userName).result.headOption
      val emailQ = compiledUserFromEmail(user.account.email).result.headOption
      val outerAggregate = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield (x, y)
      outerAggregate.map {
        
        //invalid registration --> complete with value
        case (Some(x), _) => p.success(SignupOutcome(Right(UserNameAlreadyTaken(x.userName))))
        case (_, Some(x)) => p.success(SignupOutcome(Right(EmailAddressAleadyRegistered(user.account.email))))
        
        //valid new user --> complete with this future user instead
        // if for whichever 
        case _ => p.completeWith(foldNewUser(user) flatMap {
          case (ur: UserRow) => fullUserFromId(ur.id)
        } map {
          case Some(success) => SignupOutcome(Left(success))
          case None          => SignupOutcome(Right(UnexpectedSignupError("failed to signup user: " + user)))
        })
      }
      p.future
    }

    /**
     * Save user and all optional sign up information
     * @param user
     * @return
     */
    private def foldNewUser(user: User): Future[UserRow] = {
      import ModelImplicits.ToDataRow.{ rowFromUserNameAndForeignKeys, rowFromAccount }
      val accF = cake.commit(AccountRepo.save(rowFromAccount(user.account)))

      val extras = (
        user.visitor map { v => handleVisitor(v) },
        user.userprofile map { up => handleUserProfile(up) })

      extras match {

        //both visitor and user profile are handled
        case (Some(vF), Some(upF)) => {
          val aggregate = for {
            accR <- accF
            vis <- vF
            up <- upF
          } yield (accR, vis, up)
          aggregate flatMap {
            case (acc, vis, up) => {
              cake.commit(UserRepo.save(rowFromUserNameAndForeignKeys(user.userName, acc.id, Some(up.id), Some(vis.id), None)))
            }
          }
        }

        //only visitor is handled
        case (Some(vF), None) => {
          val aggregate = for {
            accR <- accF
            vis <- vF
          } yield (accR, vis)
          aggregate flatMap {
            case (acc, vis) => {
              cake.commit(UserRepo.save(rowFromUserNameAndForeignKeys(user.userName, acc.id, None, Some(vis.id), None)))
            }
          }
        }

        //neither is handled, only map account
        case _ => accF flatMap { acc => cake.commit(UserRepo.save(rowFromUserNameAndForeignKeys(user.userName, acc.id, None, None, None))) }
      }

    }

    def visitorFromOption(id: Option[Long]) = {
      id map { x => cake.runAction(VisitorRepo.findById(x)) }
    }

    def fullUserFromId(id: Long): Future[Option[User]] = {
      val sqlAction = fullUserFromIdQuery(id).result.headOption
      val q = cake.runAction(sqlAction)
      q.map { maybe => maybe.map(x => ModelImplicits.ToModel.userFromRows(x._1, x._2, x._3, x._4)) }
    }

    /**
     * Create a future of optional user based on provided user name or email address.
     * @param key
     * @return
     */
    def userFromEitherUserNameOrEmail(key: String): Future[Option[User]] = {
      val userNameQ = compiledUserFromUserName(key).result.headOption
      val emailQ = compiledUserFromEmail(key).result.headOption
      val p = Promise[Option[User]]()
      val aggregateFuture = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield (x, y)
      aggregateFuture.map {
        case (Some(x), None) => p.completeWith(fullUserFromId(x.id))
        case (None, Some(x)) => p.completeWith(fullUserFromId(x.id))
        case _               => p.success(None)
      }
      p.future
    }

    /**
     * Create a future of {@VisitorRow}. If visitor host exists then update its timestamp.
     * @param visitor
     * @return
     */
    def handleVisitor(visitor: Visitor): Future[VisitorRow] = {
      val visitorF = cake.runAction {
        compiledVisitorFromHost(visitor.host).result.headOption
      }
      visitorF.flatMap {
        case Some(visitor) => cake.commit(VisitorRepo.save(visitor.copy(timestamp = System.currentTimeMillis())))
        case None          => cake.commit(VisitorRepo.save(ModelImplicits.ToDataRow.rowFromVisitor(visitor)))
      }
    }

    /**
     * Create a future of {@link UserProfileRow}. If UserProfile exists, return it, if not then save it.
     * @param userProfile
     * @return
     */
    def handleUserProfile(userProfile: UserProfile): Future[UserprofileRow] = {
      val userProfileF = cake.runAction {
        compiledUserProfileFromAllParams(userProfile.firstName, userProfile.lastName, userProfile.country, userProfile.placeOfRes, userProfile.age).result.headOption
      }
      userProfileF.flatMap { 
        case Some(userProfile) => Future(userProfile)
        case None => cake.runAction(UserprofileRepo.save(ModelImplicits.ToDataRow.rowFromUserProfile(userProfile)))
      }
    }

  }
}