package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.api.model.User
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import com.blueskiron.bilby.io.db.codegen.Tables.UsersRow
import com.blueskiron.bilby.io.api.model.Role
import com.blueskiron.bilby.io.db.codegen.Tables
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.postgresql.slick.Driver
import scala.language.postfixOps

/**
 * This data access object handles all queries and statements pertinent to {@link com.blueskiron.bilby.io.api.model.User}
 * @author juri
 *
 */
trait UserDao {

  /**
   * Initialize this dao trait with a specific instance of ApplicationDatabase.
   * Pass implicit execution context within which all futures are scheduled.
   * (defaults to PostgresDatabase)
   * @param cake
   * @return
   */
  def initWithApplicationDatabase[T <: PostgresDatabase](cake: T = PostgresDatabase)(implicit ex: ExecutionContext) = new UserDao(cake)

  /**
   * @return initialized UserDao with a default ApplicationDatabase
   * and {@link scala.concurrent.ExecutionContext.Implicits.global} context.
   */
  lazy val userDao = {
    import scala.concurrent.ExecutionContext.Implicits.global
    initWithApplicationDatabase[PostgresDatabase]()
  }

  protected class UserDao[T](val cake: PostgresDatabase)(implicit ex: ExecutionContext) {

    import cake.jdbcProfile.api._

    /**
     * Updates specified user. If the given user does not exists, this update fails.
     * @param u
     * @return
     */
    def updateUser(u: User): Future[User] = {
      import ModelImplicits.ToModel
      updateUserRow(u) map ToModel.userFromRow
    }

    private def updateUserRow(u: User): Future[UsersRow] = {
      import ModelImplicits.ToDataRow
      cake.commit(UsersRepo.update(ToDataRow.rowFromUser(u)))
    }

    /**
     * Sets username for the given user id.
     * @param userId
     * @param username
     * @return
     */
    def setUserName(userId: Long, username: Option[String]): Future[User] = {
      import ModelImplicits.ToModel
      val _username = username
      cake.runAction(UsersRepo.findById(userId)) flatMap { model =>
        val copied = model.copy(username = _username)
        cake.runAction(UsersRepo.save(copied))
      } map ToModel.userFromRow
    }

    def addRole(userId: Long, role: Role): Future[User] = {
      import ModelImplicits.ToModel
      val _role = role
      cake.runAction(UsersRepo.findById(userId)) flatMap { model =>
        val _roles = model.roles ++ Set(_role.name)
        val copied = model.copy(roles = _roles)
        cake.runAction(UsersRepo.save(copied))
      } map ToModel.userFromRow
    }

    def findUserByUserName(username: String): Future[Option[User]] = {
      import ModelImplicits.ToModel
      for (ur <- cake.runAction(compiledUserFromUserName(Some(username)).result.headOption))
        yield ur map ToModel.userFromRow _
    }

    def findUserByProfile(linfo: LoginInfo): Future[Option[User]] = {
      import ModelImplicits.ToModel
      val repMap = Map(linfo.providerID -> linfo.providerKey)
      for (ur <- cake.runAction(compiledUserFromProfile(repMap).result.headOption))
        yield ur map ToModel.userFromRow _
    }

    private def userFromUserNameQuery(userName: Rep[Option[String]]) = {
      for {
        (u) <- Tables.Users if (u.username === userName)
      } yield u
    }

    private def userFromProfileQuery(linfo: Rep[Map[String, String]]) = {
      for (u <- Tables.Users.filter(_.profiles @> linfo)) yield u
    }

    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val compiledUserFromUserName = Compiled(userFromUserNameQuery _)
    /**
     * Compiled query for getting {@link UserRow} by profile ({@link LoginInfo})
     */
    val compiledUserFromProfile = Compiled(userFromProfileQuery _)

  }
}