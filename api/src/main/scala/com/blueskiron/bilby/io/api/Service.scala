package com.blueskiron.bilby.io.api

import com.blueskiron.bilby.io.api.model.User

object UserService {
  
  /*
   * auth actor communication
   */
  type UserCredentials = (String, String)
  case class AuthRequest(secret: UserCredentials)
  case class AuthResponse(user: Option[User])
  
  /*
   * signup actor communication 
   */
  case class SignupRequest(user: User)
  type SignupOutcome = Either[User, SignupRejection]
  sealed trait SignupRejection
  case class UserNameAlreadyTaken(userName: String) extends SignupRejection
  case class EmailAddressAleadyRegistered(email: String) extends SignupRejection

}

object PieceService {
  
}

object FollowerService {
  
}

  