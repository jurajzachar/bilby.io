package controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import components.UserBindings
import components.UserComponent
import models.User
import models.UserProfile
import models.Visitor
import play.api._
import play.api.data.Form
import play.api.mvc._
import play.api.mvc.Controller
import play.api.mvc.Security
import controllers.Auth.CSFRHelper

object Signup extends Controller with UserBindings {

  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  /**
   * Display an empty form.
   */
  def init = CSFRHelper.withToken { implicit request =>
    Ok(views.html.signup.init(form));
  }

  def submit = Action {
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
            case Left(user)    => Ok(views.html.account.summary(user, Some(userCombo._2)))
            case Right(reject) => BadRequest(views.html.signup.init(form.fill(null, null)))
          }
        })
  }

  def change = Action {
    Ok("TODO")
  }
}