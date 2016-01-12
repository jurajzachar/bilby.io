package com.blueskiron.bilby.io.api.service

import com.blueskiron.bilby.io.api.model.User
import play.mvc.Http.RequestHeader
import play.api.mvc.Request
import com.blueskiron.bilby.io.api.model.UserProfile
import play.api.mvc.Result

trait RegistrationService {

  case class RegistrationData(
    username: String,
    password: String,
    email: String,
    profile: UserProfile)
    
  /**
   * Used to pass data to actor that registers new user
   * @author juri
   *
   */
  abstract class RegistrationRequest[+A] {
    
    /**
     * @return
     */
    def data: RegistrationData 
    
    /**
     * @return
     */
    def request: Request[A]
    
    /**
     * @return Result that is used to embed A's authenticated result
     */
    def onSuccess: Result
    
    /**
     * @return function that is used to process registration failure
     */
    def onFailure[B <: RegistrationRejection]: B => Result
    
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
   * hellper factory call when registration fails
   * @param reject
   */
  def outcomeFromRejection(reject: RegistrationRejection) = RegistrationOutcome(Right(reject))
  /**
   * hellper factory call when registration succeeds
   * @param reject
   */
  def outcomeFromUser(user: User) = RegistrationOutcome(Left(user))
}