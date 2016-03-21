package com.blueskiron.bilby.io.db.service

import com.blueskiron.bilby.io.db.PostgresDatabase
import javax.inject.{ Singleton, Inject }
import com.blueskiron.bilby.io.db.dao.UserDao
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.mohiva.play.silhouette.impl.providers.SocialProfile
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.api.model._
import com.mohiva.play.silhouette.api.services.IdentityService
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import org.slf4j.LoggerFactory
import play.api.UnexpectedException
import scala.concurrent.Promise
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.bilby.io.api.RegistrationService
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders.CREDENTIALS

@Singleton
class UserService[T <: PostgresDatabase] @Inject() (protected val cake: T)
    extends IdentityService[User] 
    with RegistrationService
    with UserDao {
    
  implicit val executionContext: ExecutionContext = cake.database.executor.executionContext
  
  lazy protected val userDao = initUserDao(cake)

  override def retrieve(loginInfo: LoginInfo) = userDao.findOptionUser(loginInfo)

  private def checkNativeProfiles[U >: RegistrationOutcome](u: User, profile: UserProfile): Future[U] = {
    //check if email was already used in previous registrations
    if (profile.loginInfo.providerID.equals(CREDENTIALS.id)) {
      val nativeChecks = for {
        checkEmail <- userDao.findUserProfile(profile.email)
        checkUserName <- userDao.findOptionUser(u.username)
      } yield (checkEmail, checkUserName)
      nativeChecks.flatMap {
        case (Some(profile), _) => Future.successful(outcomeFromRejection(EmailAddressAlreadyRegistered(profile.email.get)))
        case (_, Some(user))    => Future.successful(outcomeFromRejection(UserNameAlreadyRegistered(user.username)))
        case (None, None)       => Future.successful(outcomeFromUser(u))
      }
    } else {
      Future.successful(outcomeFromUser(u))
    }
  }

  /**
   * @return total count of all users
   */
  def count = userDao.count

  /**
   * Creation of a new user
   * @param u
   * @param profile
   * @return
   */
  def create[S <: UserProfile, U >: RegistrationOutcome](u: User, profile: UserProfile): Future[U] = {
    //check if username exists
    userDao.findOptionUser(u.username) flatMap {
      case Some(existingUser) => {
        Future.successful(outcomeFromRejection(UserAlreadyRegistered(u)))
      }
      case None => {
        //if NATIVE, check if email or username was already used in previous registrations
        checkNativeProfiles(u, profile) flatMap { outcome =>
          outcome.result match {
            case Right(reject) => Future.successful(outcome)
            case Left(user)    => handle(user, profile, true).map { outcomeFromUser(_) }
          }
        }
      }
    }
  }

  /**
   * Handle user creation and profile updates
   * @param currentUser
   * @param profile
   * @return
   */
  def handle[S <: UserProfile](currentUser: User, profile: S, nativeProfileChecked: Boolean): Future[User] = {
    if (nativeProfileChecked) {
      val agg = for {
        hasProfile <- retrieve(profile.loginInfo)
        hasUserName <- userDao.findOptionUser(currentUser.username)
      } yield (hasProfile, hasUserName)
      agg.flatMap {
        case (None, None)    => userDao.create(currentUser, profile)
        //this should have never happened... F
        case (Some(u), None) => Future.failed(new Exception("updating unkown user with existing profile is prohibited."))
        case (_, Some(u))    => userDao.update(u, profile)
      }
    } else {
      checkNativeProfiles(currentUser, profile).flatMap { outcome =>
        outcome.result match {
          case Right(reject) => Future.successful(currentUser)
          case Left(user)    => handle(user, profile, true)
        }
      }
    }
  }

  /**
   * Removes user from the authentication scope. Deactivated users are garbage collected from the database after given period of time.
   * @param currentUser
   * @return
   */
  def deleteUser(currentUser: User): Future[User] = userDao.deactivate(currentUser)

}