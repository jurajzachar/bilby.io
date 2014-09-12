package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.{ User, UserProfile }
import views._
import org.slf4j.{ LoggerFactory, Logger }
import org.mindrot.jbcrypt.BCrypt

object SignUp extends Controller {

  val logger: Logger = LoggerFactory.getLogger(this.getClass())
  /**
   * Sign Up Form definition.
   *
   * Once defined it handle automatically, ,
   * validation, submission, errors, redisplaying, ...
   */
  val signupForm: Form[User] = Form(

    // Define a mapping that will handle User values
    mapping(
      "username" -> text(minLength = 4),
      "email" -> email,

      // Create a tuple mapping for the password/confirm
      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text).verifying(
          // Add an additional constraint: both passwords must match
          "Passwords don't match", passwords => passwords._1 == passwords._2),

      // Create a mapping that will handle UserProfile values
      "profile" -> mapping(
        "country" -> nonEmptyText,
        "city" -> optional(text),
        "age" -> optional(number(min = 18, max = 100))) // The mapping signature matches the UserProfile case class signature,
        // so we can use default apply/unapply functions here
        (UserProfile.apply)(UserProfile.unapply),
      "accept" -> checked("You must accept the conditions"), // The mapping signature doesn't match the User case class signature,
      "edited" -> boolean) // so we have to define custom binding/unbinding functions
      {
        // Binding: Create a User from the mapping result (ignore the second password and the accept field)
        (username, email, passwords, profile, _, edited) =>
          {
            val user = User(username, BCrypt.hashpw(passwords._1, BCrypt.gensalt()), email, 0, profile)
            user.edited = edited
            user
          }
      } {
        // Unbinding: Create the mapping values from an existing User value
        user => Some(user.username, user.email, (user.password, ""), user.profile, user.edited, user.edited)
      }.verifying(
        // Add an additional constraint: The username must not be taken (you could do an SQL request here)
        "This username is not available.",
        user => if (!user.edited)
          !User.findAll.map(user => user.username).toSeq.contains(user.username)
        else true)) //end of form

  /**
   * Display an empty form.
   */
  def form = Action {
    Ok(html.signup.form(signupForm));
  }

  /**
   * Display a form pre-filled with an existing User.
   */
  def editForm = Action {
    val existingUser = User.findAll.toList.head
    existingUser.edited = true
    existingUser.profile = UserProfile.findById(existingUser.userprofile_id).getOrElse(null)
    Ok(html.signup.edit_user(signupForm.fill(existingUser)))
  }

  /**
   * Handle form submission.
   */
  def submit = Action { implicit request =>
    {
      request.body.asFormUrlEncoded.getOrElse(Map()).exists(_ == "edited" -> true) match {
        //user is being edited   
        case true =>
          {
            signupForm.bindFromRequest.fold(
              // Form has errors, redisplay it
              errors => BadRequest(html.signup.edit_user(errors)),
              // We got a valid User value, display the summary
              user => {
                User.registerNewUser(user)
                Ok(html.signup.summary(user))

              })
          }

        case false => {
          signupForm.bindFromRequest.fold(
            errors => BadRequest(html.signup.form(errors)),
            user => {
              if (user.edited)
                User.update(user)
              Ok(html.signup.summary(user))
            })
        }
      }
    }
  }
}