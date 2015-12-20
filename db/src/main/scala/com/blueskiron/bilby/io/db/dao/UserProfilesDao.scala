package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.PostgresDatabase
import javax.management.relation.Role
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.bilby.io.db.codegen.Tables
import scala.concurrent.ExecutionContext
import com.blueskiron.bilby.io.api.model.User
import scala.concurrent.Future
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.blueskiron.bilby.io.api.model.UserProfile

trait UserProfilesDao {
  /**
   * Initialize this dao trait with a specific instance of ApplicationDatabase.
   * Pass implicit execution context within which all futures are scheduled.
   * (defaults to PostgresDatabase)
   * @param cake
   * @return
   */
  def initWithApplicationDatabase[T <: PostgresDatabase](cake: T = PostgresDatabase)(implicit ex: ExecutionContext) = new UserProfilesDao(cake)

  /**
   * @return initialized UserDao with a default ApplicationDatabase
   * and {@link scala.concurrent.ExecutionContext.Implicits.global} context.
   */
  lazy val userProfilesDao = {
    import scala.concurrent.ExecutionContext.Implicits.global
    initWithApplicationDatabase[PostgresDatabase]()
  }

  protected class UserProfilesDao[T](val cake: PostgresDatabase)(implicit ex: ExecutionContext) {

    import cake.jdbcProfile.api._
    
    def findUserProfile(provider: String, key: String): Future[Option[UserProfile]] = {
      import ModelImplicits.ToModel
      for (upr <- cake.runAction(compiledUserProfileFromPrimaryKey(provider, key).result.headOption))
        yield upr map ToModel.userProfileFromRow _
    }

    def findUserProfiles(username: String): Future[Set[UserProfile]] = {
      import ModelImplicits.ToModel
      for (profiles <- cake.runAction(compiledUserProfilesFromUserName(username).result))
        yield profiles.toSet map ToModel.userProfileFromRow _
    }

    /*
     * select username, provider, key from users, user_profiles where username = 'ahernandez1' and profiles -> provider = key;
     */
    private def userProfilesFromUsernameQuery(username: Rep[String]) = {
      for {
        u <- Tables.Users
        ups <- Tables.UserProfiles filter (upr => u.profiles.+>(upr.provider) === upr.key)
        if(u.username === username)
      } yield ups

    }

    private def userProfileFromPrimaryKeyQuery(provider: Rep[String], key: Rep[String]) = {
      for (up <- Tables.UserProfiles.filter(row => row.provider === provider && row.key === key)) yield up
    }

    /**
     * Compiled query for getting {@link UserRow} by profile ({@link LoginInfo})
     */
    val compiledUserProfileFromPrimaryKey = Compiled(userProfileFromPrimaryKeyQuery _)

    /**
     * Compiled query for getting {@link UserProfilesRow} by username ({@link User})
     */
    val compiledUserProfilesFromUserName = Compiled(userProfilesFromUsernameQuery _)
  }
}