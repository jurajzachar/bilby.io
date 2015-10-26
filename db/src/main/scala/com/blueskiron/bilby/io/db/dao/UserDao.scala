package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.Tables
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.blueskiron.bilby.io.db.Tables.{UserRow, VisitorRow}
import scala.concurrent.Promise
import com.blueskiron.bilby.io.model.{Visitor, UserProfile, User}
import com.blueskiron.bilby.io.db.ar.ModelImplicits._
import com.blueskiron.bilby.io.db.ActiveSlickRepos.VisitorRepo

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

  class UserDao(val cake: ApplicationDatabase) {

    import cake.jdbcProfile.api._

    private val emailRe = """(\w+)@([\w\.]+)""".r
    val log = LoggerFactory.getLogger(this.getClass)
    val emailTaken = "This email address (%s) address is already taken"
    val usernameTaken = "This username (%s) is already taken"
    val invalidLogin = "Wrong combination of username/email address and password"

    private def userFromEmailQuery(email: Rep[String]) = {
      for (u <- Tables.User if u.email === email) yield u
    }

    private def userFromUserNameQuery(email: Rep[String]) = {
      for (u <- Tables.User if u.userName === email) yield u
    }
    
    private def visitorFromHostQuery(host: Rep[String]) = {
      for(v <- Tables.Visitor if v.host === host) yield v
    }
    
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
     * Create a future of optional user based on provided username or email address.
     * @param key
     * @return
     */
    def userFromEitherUserNameOrEmail(key: String): Future[Option[User]] = {
      val userNameQ = userFromUserName(key).result.headOption
      val emailQ = userFromEmail(key).result.headOption
      //cake.runAction(userNameQ).on
      val p =  Promise[Option[User]]()
      val aggregateFuture = for {
        x <- cake.runAction(userNameQ)
        y <- cake.runAction(emailQ)
      } yield(x, y)
      aggregateFuture.map{
        case (Some(x), None) => p.success(Some(userFromRows(x, None, None)))
        case (None, Some(x)) => p.success(Some(userFromRows(x, None, None)))
        case _ => p.success(None)
      }
      p.future
    }
    
    def handleVisitor(visitor: Visitor) = {
      val visitorF = cake.runAction {
        visitorFromHost(visitor.host).result.headOption
      }
      visitorF.flatMap { 
        //commit(UserRepo.save(savedUser.copy(password = _password)))
        case Some(visitor) => cake.commit(VisitorRepo.save(visitor.copy(timestamp = System.currentTimeMillis())))
        case None => cake.commit(VisitorRepo.save(visitorRowFromVisitor(visitor)))
      }
    }
  }
}