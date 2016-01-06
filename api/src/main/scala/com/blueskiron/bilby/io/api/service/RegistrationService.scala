package com.blueskiron.bilby.io.api.service

import com.blueskiron.bilby.io.api.model.User
import play.mvc.Http.RequestHeader

trait RegistrationService {

  /**
   * Used to pass data to actor that registers new user
   * @author juri
   *
   */
  case class RegistrationRequest(data: RegistrationData, request: RequestHeader)

  case class RegistrationData(
    username: String,
    email: String,
    password: String)
  /**
   * Response sent upon user registration
   * @author juri
   *
   */
  trait RegistrationResponse

  case class RegistrationOutcome(result: Either[User, RegistrationRejection]) extends RegistrationResponse

  abstract class RegistrationRejection

  case class UserAlreadyRegistered(user: User) extends RegistrationRejection

  case class UserNameAlreadyTaken(username: String) extends RegistrationRejection

  case class EmailAddressAleadyRegistered(email: String) extends RegistrationRejection

  /**
   * hellper factory call
   * @param reject
   */
  def outcomeFromRejection(reject: RegistrationRejection) = RegistrationOutcome(Right(reject))
  /**
   * hellper factory call
   * @param reject
   */
  def outcomeFromUser(user: User) = RegistrationOutcome(Left(user))
}