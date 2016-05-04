package forms

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import com.blueskiron.bilby.io.db.service.UserService
import com.blueskiron.bilby.io.db.PostgresDatabase
import scala.concurrent.Await

object UserForms {

  sealed case class UserRegistrationForm(
    username: String,
    email: String,
    password: (String, String),
    firstName: Option[String],
    lastName: Option[String],
    country: Option[String],
    city: Option[String],
    age: Option[Int],
    termsAgreed: Boolean)

  def registrationForm(userService: UserService[PostgresDatabase]) = Form(mapping(
    "username" -> nonEmptyText(6, 10),
    "email" -> email,
    // Create a tuple mapping for the password/confirm
    "password" -> tuple(
      "main" -> nonEmptyText(8, 32),
      "confirm" -> text).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", Password => Password._1 == Password._2),
    //optional...
    "firstName" -> optional(text),
    "lastName" -> optional(text),
    "country" -> optional(text),
    "city" -> optional(text),
    "age" -> optional(number),
    "termsAgreed" -> checked("You must agree to the terms and conditions... blah blah"))(UserRegistrationForm.apply)(UserRegistrationForm.unapply))

  val signInForm = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText(8, 32)))

  object Countries {
    val list = java.util.Locale.getISOCountries().map(cc => cc -> (new java.util.Locale("", cc).getDisplayCountry()))
  }
}