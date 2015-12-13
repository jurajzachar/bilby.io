package com.blueskiron.bilby.io.api

//import com.blueskiron.bilby.io.api.model.User

object UserService {
  
  type UserCredentials = (String, String)
  
  sealed trait Request
  case class Authenticate(secret: UserCredentials) extends Request
  //case class Signup(user: User) extends Request
  //case class Deactivate(user: User)extends Request

  sealed trait Response
  //case class SignupOutcome(result: Either[User, SignupRejection]) extends Response 
  sealed trait SignupRejection 
  case class UserNameAlreadyTaken(userName: String) extends SignupRejection
  case class EmailAddressAleadyRegistered(email: String) extends SignupRejection
  case class UnexpectedSignupError(error: String) extends SignupRejection
  
}

object PieceService {
  
}

object FollowerService {
  
}

  