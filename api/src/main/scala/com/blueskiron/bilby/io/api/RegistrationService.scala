package com.blueskiron.bilby.io.api

import com.blueskiron.bilby.io.api.model.User
import play.api.mvc.RequestHeader
import com.blueskiron.bilby.io.api.model.UserProfile
import play.api.mvc.Result
import scala.Left
import scala.Right
import scala.concurrent.Promise
import com.mohiva.play.silhouette.api.services.AuthenticatorResult

trait RegistrationService extends ConfiguredService {

  case class RegistrationData(
    user: User,
    profile: UserProfile,
    email: String,
    password: String)
    
  /**
   * Used to pass data to actor that registers a new user
   * @author juri
   *
   */
  abstract class RegistrationRequest {
    
    /**
     * @return
     */
    val data: RegistrationData 
    
    /**
     * @return
     */
    val header: RequestHeader
    
    /**
     * @return Result that is used to embed A's authenticated result
     */
    val onSuccess: Result
    
    /**
     * @return function that is used to process registration failure
     */
    val result = Promise[AuthenticatorResult]()
  }
  
  /**
   * Response sent upon user registration
   * @author juri
   *
   */
  abstract class RegistrationException(msg: String, cause: Throwable = null) extends Exception(msg, cause)

  case class RegistrationOutcome(result: Either[User, RegistrationException])

  case class UserAlreadyRegistered(user: User) 
    extends RegistrationException("registration.service.user.already.registered")

  case class UserNameAlreadyRegistered(username: String) 
    extends RegistrationException("registration.service.username.taken")

  case class EmailAddressAlreadyRegistered(email: String) 
    extends RegistrationException("registration.service.email.registered")

  /**
   * helper factory call when registration fails
   * @param reject
   */
  def outcomeFromException(reject: RegistrationException) = RegistrationOutcome(Right(reject))
  /**
   * helper factory call when registration succeeds
   * @param reject
   */
  def outcomeFromUser(user: User) = RegistrationOutcome(Left(user))
}

object RegistrationService extends RegistrationService 
