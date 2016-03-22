package models

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data._
import play.api.data.Forms._

object UserForms {
  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )

  val registrationForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> email.verifying(maxLength(250)),
      "password" -> nonEmptyText.verifying(minLength(8))
    )(RegistrationData.apply)(RegistrationData.unapply)
  )
}