package com.blueskiron.bilby.io.db.service

import com.blueskiron.bilby.io.api.UserService._
import com.blueskiron.bilby.io.db.PostgresDatabase
import javax.inject.{ Singleton, Inject }
import com.blueskiron.bilby.io.db.dao.UserDao
import scala.concurrent.ExecutionContext
import com.blueskiron.bilby.io.api.model.User
import scala.concurrent.Future
import com.mohiva.play.silhouette.impl.providers.SocialProfile
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.api.model.UserProfile
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.bilby.io.api.UserService.UserAlreadyRegistered
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders.NATIVE
import com.blueskiron.bilby.io.api.UserService.EmailAddressAleadyRegistered
import com.blueskiron.bilby.io.api.UserService.UserNameAlreadyTaken
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import org.slf4j.LoggerFactory
import play.api.UnexpectedException
import scala.concurrent.Promise
import com.blueskiron.bilby.io.api.UserService.Response

@Singleton
class UserService[T <: PostgresDatabase] @Inject() (override protected val cake: T)(implicit ex: ExecutionContext) extends IdentityService[User] with ClosableDatabase[T]
    with UserDao {

  private val log = LoggerFactory.getLogger(getClass)
  
  lazy protected val userDao = initUserDao(cake)

  override def retrieve(loginInfo: LoginInfo) = userDao.findOptionUser(loginInfo)

  private def checkNativeProfiles[U >: Response](u: User, profile: UserProfile): Future[Either[User, U]] = {
    //check if email was already used in previous registrations
    if (profile.loginInfo.providerID.equals(NATIVE.id)) {
      val nativeChecks = for {
        checkEmail <- userDao.findUserProfile(profile.email)
        checkUserName <- userDao.findOptionUser(u.username)
      } yield (checkEmail, checkUserName)
      nativeChecks.flatMap {
        case (Some(profile), _) => Future.successful(Right(EmailAddressAleadyRegistered(profile.email.get)))
        case (_, Some(user))    => Future.successful(Right(UserNameAlreadyTaken(user.username)))
        case (None, None)       => Future.successful(Left(u))
      }
    } else {
      Future.successful(Left(u))
    }
  }
  
  def count = userDao.count
  
  /**
   * Sign up of a new user
   * @param u
   * @param profile
   * @return
   */
  def create[S <: UserProfile, U >: Response](u: User, profile: UserProfile): Future[Either[User, U]] = {
    //check if username exists
    userDao.findOptionUser(u.username) flatMap {
      case Some(existingUser) => {
        Future.successful(Right(UserAlreadyRegistered(u)))
      }
      case None => {
        //if NATIVE, check if email or username was already used in previous registrations
        checkNativeProfiles(u, profile).flatMap {
          case Right(reject) => Future.successful(Right(reject))
          case Left(user)    => handle(user, profile, true).map { Left(_) }
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
        case (None, None)    => getFutureValueOrFail(userDao.create(currentUser, profile)) 
        //this should have never happened... F
        case (Some(u), None) => Future.failed(new Exception("updating unkown user with existing profile is prohibited."))
        case (_, Some(u)) => getFutureValueOrFail(userDao.update(u, profile))
      }
    } else {
      checkNativeProfiles(currentUser, profile).flatMap {
        case Right(reject) => Future.successful(currentUser)
        case Left(user)    => handle(user, profile, true)
      }
    }
  }

  //Wrapper call that yields value from option fails fast
  private def getFutureValueOrFail[U](f: Future[Option[U]]): Future[U] = {
    f.flatMap {
      case Some(x) => Future.successful(x)
      case None => { //emit warning
        Future.failed(new Exception("operation: " + f + " has failed!"))
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