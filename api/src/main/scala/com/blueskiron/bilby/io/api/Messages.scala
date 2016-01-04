package com.blueskiron.bilby.io.api

import com.blueskiron.bilby.io.api.model.User

//import com.blueskiron.bilby.io.api.model.User

object ActorMessages {
  
  object CloseAndDie //will send PoisonPill and close database 
  
}

object UserService {
  
  type UserCredentials = (String, String)
  
  sealed trait Request
  case class Authenticate(secret: UserCredentials) extends Request
  case class Signup(user: User) extends Request
  case class Deactivate(user: User) extends Request

  trait Response
  case class UnexpectedSystemError(t: Throwable) extends Response  
  
  trait SignupRejection extends Response 
  case class SignupOutcome(result: Either[User, SignupRejection]) extends Response 
  case class UserAlreadyRegistered(user: User) extends SignupRejection
  case class UserNameAlreadyTaken(username: String) extends SignupRejection
  case class EmailAddressAleadyRegistered(email: String) extends SignupRejection

}

object AssetService {
  //todo
}

object FollowerService {
  //todo
}

  