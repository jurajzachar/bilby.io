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
    val onFailure: RegistrationRejection => Result
    
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
  abstract class RegistrationResponse

  case class RegistrationOutcome(result: Either[User, RegistrationRejection]) extends RegistrationResponse

  abstract class RegistrationRejection(messageKey: String)

  case class UserAlreadyRegistered(user: User) 
    extends RegistrationRejection("registration.service.user_already_registered")

  case class UserNameAlreadyRegistered(username: String) 
    extends RegistrationRejection("registration.service.username_already_taken")

  case class EmailAddressAlreadyRegistered(email: String) 
    extends RegistrationRejection("registration.service.email_address_already_registered")

  /**
   * helper factory call when registration fails
   * @param reject
   */
  def outcomeFromRejection(reject: RegistrationRejection) = RegistrationOutcome(Right(reject))
  /**
   * helper factory call when registration succeeds
   * @param reject
   */
  def outcomeFromUser(user: User) = RegistrationOutcome(Left(user))
}

object RegistrationService extends RegistrationService 
