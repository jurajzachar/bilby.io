package com.blueskiron.bilby.io.db.dao

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.postfixOps
import com.blueskiron.bilby.io.api.model.Role
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.UserProfile
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.db.codegen.ModelImplicits.{ ToDataRow, ToModel }
import com.blueskiron.bilby.io.db.codegen.Tables
import com.blueskiron.bilby.io.db.codegen.Tables.UsersRow
import com.blueskiron.postgresql.slick.Driver
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure

/**
 * This data access object handles all queries and statements pertinent to
 *  {@link com.blueskiron.bilby.io.api.model.User} and {@link com.blueskiron.bilby.io.api.model.UserProfile}.
 *  In doing so, it fully relies on compiled slick queries that join tables using Postgresql's HSTORE extension.
 *
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
  def initUserDao[T <: PostgresDatabase](cake: T) = new UserDao(cake)(cake.executionContext)

  protected class UserDao[+T](val cake: PostgresDatabase)(implicit ex: ExecutionContext) {

    import cake.jdbcProfile.api._

    /**
     * @return total count of all users
     */
    def count: Future[Int] = {
      cake.runAction(UsersRepo.count)
    }

    /**
     * Creates specified user. If the given user or profile already exists, this operation fails.
     * @param user, profile
     * @return
     */
     def create(user: User, profile: UserProfile): Future[User] = {
      import ToDataRow.{ rowFromUserProfile, rowFromUser }
      val userWithProfile = user.copy(
        profiles = user.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo)
      //persist both transactionally
      val saveAction = for {
        profile <- Tables.UserProfiles += profile
        user <- UsersRepo.save(user)
      } yield user
      cake.commit(saveAction) map ToModel.userFromRow _  
//       val action = DBIO.seq(
//        Tables.UserProfiles += profile,
//        UsersRepo.save(user)).transactionally
//      cake.runAction(action.andThen(retrieveAction(profile.loginInfo))) map ToModel.userFromRow _
    }

    /**
     * Updates specified user. If the given user does not exists, this operation fails.
     * @param u
     * @return
     */
    def update(user: User, profile: UserProfile): Future[User] = {
      import ToDataRow.{ rowFromUserProfile, rowFromUser }
      val userWithProfile = user.copy(
        profiles = user.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo)
      println(s"updating $user with $userWithProfile")
      val tx = findUserProfile(profile.loginInfo.providerID, profile.loginInfo.providerKey).map {
        case Some(p) => {
          DBIO.seq(
            UsersRepo.update(userWithProfile),
            Tables.UserProfiles.update(profile)).transactionally
        }
        case None => {
          DBIO.seq(
            UsersRepo.update(userWithProfile),
            Tables.UserProfiles += profile).transactionally
        }
      }
      val userRowFuture = tx.flatMap { updateAction => cake.runAction(updateAction.andThen(retrieveAction(profile.loginInfo))) }
      userRowFuture map ToModel.userFromRow _
    }

    private def retrieveAction(linfo: LoginInfo) = {
      val repMap = Map(linfo.providerID -> linfo.providerKey)
      compiledUserFromProfile(repMap).result.head
    }

    def deactivate(user: User): Future[User] = {
      import ToDataRow.rowFromUser
      val deactivated = user.copy(active = false)
      cake.commit(UsersRepo.update(deactivated)) map ToModel.userFromRow _
    }

    /**
     * Sets username for the given user id. This should be disallowed for native profiles
     * @param userId
     * @param username
     * @return
     */
    def setUserName(userId: Long, username: String): Future[User] = {
      import ToModel.userFromRow
      val _username = username
      cake.runAction(UsersRepo.findById(userId)) flatMap { model =>
        {
          if (model.profiles.contains(SupportedAuthProviders.CREDENTIALS.id)) Future.successful(model)
          else {
            val copied = model.copy(username = _username)
            cake.runAction(UsersRepo.save(copied))
          }
        }
      } map userFromRow
    }

    /**
     * @param userId
     * @param role
     * @return
     */
    def addRole(userId: Long, role: Role): Future[User] = {
      import ToModel.userFromRow
      val _role = role
      cake.runAction(UsersRepo.findById(userId)) flatMap { model =>
        val _roles = model.roles ++ Set(_role.name)
        val copied = model.copy(roles = _roles)
        cake.runAction(UsersRepo.save(copied))
      } map userFromRow
    }

    def checkReserved(username: String): Future[Option[String]] = {
      cake.runAction(compiledCheckReserved(username).result.headOption).flatMap { x => Future.successful(x.map(_.name)) }
    }

    /**
     * @param id
     * @return
     */
    def findUser(id: Long): Future[User] = {
      import ToModel.userFromRow
      cake.runAction(UsersRepo.findById(id)) map userFromRow _
    }

    /**
     * @param linfo
     * @return
     */
    def findUser(linfo: LoginInfo): Future[User] = {
      import ToModel.userFromRow
      val repMap = Map(linfo.providerID -> linfo.providerKey)
      for (ur <- cake.runAction(compiledUserFromProfile(repMap).result.head))
        yield userFromRow(ur)
    }

    /**
     * @param linfo
     * @return
     */
    def findOptionUser(linfo: LoginInfo): Future[Option[User]] = {
      import ToModel.userFromRow
      val repMap = Map(linfo.providerID -> linfo.providerKey)
      for (ur <- cake.runAction(compiledUserFromProfile(repMap).result.headOption))
        yield ur map userFromRow _
    }

    /**
     * @param username
     * @return
     */
    def findOptionUser(username: String): Future[Option[User]] = {
      import ToModel.userFromRow
      for (ur <- cake.runAction(compiledUserFromUserName(Some(username)).result.headOption))
        yield ur map userFromRow _
    }

    def findUserAndUserProfile(linfo: LoginInfo): Future[Option[(User, UserProfile)]] = {
      import ToModel.{ userProfileFromRow, userFromRow }
      for (combo <- cake.runAction(compiledUserAndUserProfilesFromLoginInfo(linfo.providerID, linfo.providerKey).result.headOption))
        yield combo map { x => (x._1, x._2) }
    }

    def saveUserProfile(up: UserProfile): Future[Option[UserProfile]] = {
      import ToDataRow.rowFromUserProfile
      findUserProfile(up.loginInfo.providerID, up.loginInfo.providerKey) flatMap {
        case Some(userProfile) => Future.successful(Some(userProfile))
        case None => {
          val insert = cake.runAction(Tables.UserProfiles += up)
          insert.map {
            case 1 => Some(up) //must affect one row only
            case _ => None
          }
        }
      }
    }

    def findUserProfile(email: Option[String]): Future[Option[UserProfile]] = {
      import ToModel.userProfileFromRow
      email match {
        case Some(entry) => for (upr <- cake.runAction(compiledUserProfileFromEmail(entry).result.headOption))
          yield upr map userProfileFromRow _
        case None => Future.successful(None)
      }
    }

    def findUserProfile(provider: String, key: String): Future[Option[UserProfile]] = {
      import ToModel.userProfileFromRow
      for (upr <- cake.runAction(compiledUserProfileFromPrimaryKey(provider, key).result.headOption))
        yield upr map userProfileFromRow _
    }

    def findAllUserProfiles(username: String): Future[Set[UserProfile]] = {
      import ToModel.userProfileFromRow
      for (profiles <- cake.runAction(compiledUserProfilesFromUserName(username).result))
        yield profiles.toSet map userProfileFromRow _
    }

    private def userFromUserNameQuery(username: Rep[Option[String]]) = {
      for ((u) <- Tables.Users if (u.username === username)) yield u
    }

    private def userFromProfileQuery(linfo: Rep[Map[String, String]]) = {
      for (u <- Tables.Users.filter(_.profiles @> linfo)) yield u
    }

    private def checkReservedQuery(username: Rep[String]) = {
      for (r <- Tables.Reserved.filter(_.name === username)) yield r
    }

    /*
     * select username, provider, key from users, user_profiles where   profiles -> provider = key;
     */
    private def userAndUserProfilesFromLoginInfoQuery(provider: Rep[String], key: Rep[String]) = {
      //monadic join
      for {
        u <- Tables.Users
        ups <- Tables.UserProfiles filter (row => (u.profiles.+>(row.provider)) === row.key)
      } yield (u, ups)
    }

    private def userProfilesFromUsernameQuery(username: Rep[String]) = {
      for {
        u <- Tables.Users
        ups <- Tables.UserProfiles filter (upr => u.profiles.+>(upr.provider) === upr.key)
        if (u.username === username)
      } yield ups

    }

    private def userProfileFromPrimaryKeyQuery(provider: Rep[String], key: Rep[String]) = {
      for (up <- Tables.UserProfiles.filter(row => row.provider === provider && row.key === key)) yield up
    }

    private def userProfileFromEmailQuery(email: Rep[String]) = {
      for (up <- Tables.UserProfiles.filter(row => row.email === email)) yield up
    }

    /**
     * Compiled query for checking username against reserved names
     */
    val compiledCheckReserved = Compiled(checkReservedQuery _)
    /**
     * Compiled query for getting {@link UserRow} by email address
     */
    val compiledUserFromUserName = Compiled(userFromUserNameQuery _)
    /**
     * Compiled query for getting {@link UserRow} by profile ({@link LoginInfo})
     */
    val compiledUserFromProfile = Compiled(userFromProfileQuery _)
    /**
     * Compiled query for getting {@link UserProfileRow} from email address
     */
    val compiledUserProfileFromEmail = Compiled(userProfileFromEmailQuery _)
    /**
     * Compiled query for getting {@link UserProfileRow} from ({@link LoginInfo})
     */
    val compiledUserProfileFromPrimaryKey = Compiled(userProfileFromPrimaryKeyQuery _)
    /**
     * Compiled query for getting {@link UserProfilesRow} from user name ({@link User})
     */
    val compiledUserProfilesFromUserName = Compiled(userProfilesFromUsernameQuery _)
    /**
     * Compiled query for monadic join of user and userprofile based on key => value provided.
     */
    val compiledUserAndUserProfilesFromLoginInfo = Compiled(userAndUserProfilesFromLoginInfoQuery _)

  }
}