package controllers

import components.UserBindings
import controllers.Auth.CSFRHelper
import models.Visitor
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import play.api.Logger

object Signup extends Controller with UserBindings {

  val logger: Logger = Logger(this.getClass())

  /**
   * Display an empty form.
   */
  def init = CSFRHelper.withToken { token =>
    implicit request =>
      //if a logged in user attempts to signup then he gets logged out. 
      Ok(views.html.signup.init(form)).withSession(request.session - Security.username) 
  }

  def submit = CSFRHelper.withToken { token =>
    implicit request =>
      form.bindFromRequest.fold(
        // Form has errors, redisplay it
        errors => {
          logger.debug("Signing up user returned with errors: " + errors)
          BadRequest(views.html.signup.init(errors))
        },
        // We got a valid User value, display the summary
        userCombo => {
          logger.debug("Creating a new user: " + userCombo._1.username)
          val result = dal.signUpNewUser(
            (Visitor(Some(request.remoteAddress), System.currentTimeMillis()), userCombo._2, userCombo._1))
          result match {
            case Left(user)    => Auth.createSession(
                routes.Accounter.user(user.username), user.username)
            case Right(reject) => BadRequest(views.html.signup.init(form.fill(null, null)))
          }
        })
  }

  /* TODO */
  def change = Action {
    Ok("TODO")
  }
}