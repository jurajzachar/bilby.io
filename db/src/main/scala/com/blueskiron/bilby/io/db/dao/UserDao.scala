package com.blueskiron.bilby.io.db.dao

import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Promise, Future }
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.Tables
import com.blueskiron.bilby.io.db.Tables.{ AccountRow, UserRow, UserprofileRow, VisitorRow }
import com.blueskiron.bilby.io.api.model.{ Account, Visitor, UserProfile, User }
import com.blueskiron.bilby.io.db.ActiveSlickRepos.{ AccountRepo, UserRepo, VisitorRepo, UserprofileRepo }
import com.blueskiron.bilby.io.api.UserService.{ SignupOutcome, EmailAddressAleadyRegistered, UserNameAlreadyTaken }

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
        u <- Tables.User
        a <- Tables.Account if a.email === email && u.accountId === a.id //inner join on account.id
      } yield u
    }

    private def userFromUserNameQuery(email: Rep[String]) = {
      for (u <- Tables.User if u.userName === email) yield u
    }

    private def visitorFromHostQuery(host: Rep[String]) = {
      for (v <- Tables.Visitor if v.host === host) yield v
    }
    
    private def userFromIdQuery(id: Rep[Long]) = {
      for {
        (((u, a), up), vis) <- Tables.User join Tables.Account on (_.accountId === _.id) joinLeft Tables.Userprofile on (_._1.userprofileId === _.id) joinLeft Tables.Visitor on (_._1._1.visitorId === _.id) 
        if u.id === id
      } yield (u, a, up, vis)
    }
    
    private def userProfileFromAllQuery(country: Rep[Option[String]], placeOfRes: Rep[Option[String]], age: Rep[Option[Short]]) = {
      for (up <- Tables.Userprofile if up.country === country && up.placeOfRes === placeOfRes && up.age === age)
        yield up
    }

    /* COMPILED queries */
    
    /**
     * Compiled query for getting {@link UserRow} by userId
     */
    val userFromId = Compiled(userFromIdQuery _)
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
      val emailQ = userFromEmail(user.account.email).result.headOption
      val outerAggregate = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield (x, y)
      outerAggregate.map {
        //invalid registration --> complete with value
        case (Some(x), _) => p.success(Right(UserNameAlreadyTaken(x.userName)))
        case (_, Some(x)) => p.success(Right(EmailAddressAleadyRegistered(user.account.email)))
        //valid new user --> complete with this future instead
        case _            => p.completeWith(foldNewUser(user) flatMap { case (ur: UserRow) => userAccountProfileVisitorById(ur.id) } map { Left(_) })
      }
      p.future
    }

    /**
     * Save user and all optional sign up information
     * @param user
     * @return
     */
    private def foldNewUser(user: User): Future[UserRow] = {
      import com.blueskiron.bilby.io.db.ar.ModelImplicits._
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
              cake.commit(UserRepo.save(rowFromUserAndForeignKeys(user, acc.id, Some(up.id), Some(vis.id))))
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
              cake.commit(UserRepo.save(rowFromUserAndForeignKeys(user, acc.id, None, Some(vis.id))))
            }
          }
        }

        //neither is handled, only map account
        case _ => accF flatMap { acc => cake.commit(UserRepo.save(rowFromUserAndForeignKeys(user, acc.id, None, None))) }
      }

    }

    def visitorFromOption(id: Option[Long]) = {
      id map { x => cake.runAction(VisitorRepo.findById(x)) }
    }

    def userAccountProfileVisitorById(id: Long) = {
      import com.blueskiron.bilby.io.db.ar.ModelImplicits._
      val sqlAction = userFromIdQuery(id).result.head
      val q = cake.runAction(sqlAction)
      q.map(x => User.create(Some(x._1.id), x._1.userName, accountFromRow(x._2), x._3 map userprofileFromRow, x._4 map visitorFromRow ))
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
        case (Some(x), None) => p.completeWith(userAccountProfileVisitorById(x.id) map { x => Some(x) })
        case (None, Some(x)) => p.completeWith(userAccountProfileVisitorById(x.id) map { x => Some(x) })
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
      import com.blueskiron.bilby.io.db.ar.ModelImplicits._
      val visitorF = cake.runAction {
        visitorFromHost(visitor.host).result.headOption
      }
      visitorF.flatMap {
        case Some(visitor) => cake.commit(VisitorRepo.save(visitor.copy(timestamp = System.currentTimeMillis())))
        case None          => cake.commit(VisitorRepo.save(visitor))
      }
    }

    /**
     * Create a future of {@link UserProfileRow}. If UserProfile exists, return it, if not then save it.
     * @param userProfile
     * @return
     */
    def handleUserProfile(userProfile: UserProfile): Future[UserprofileRow] = {
      import com.blueskiron.bilby.io.db.ar.ModelImplicits._
      cake.runAction(UserprofileRepo.save(userProfile))
    }

  }
}