package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import components.UserComponent

object Accounter extends Controller with Auth.Secured with UserComponent{

  /**
   * TODO
   * @return
   */
  def user(id: String) = withUser { user =>
    implicit request =>
      Ok(views.html.account.summary(user, dal.findUserProfile(user)))
  }

  /**
   * TODO
   * @return
   */
  def change = Action {
    request =>
      Ok("not implemented")
  }

  /**
   * TODO
   * @return
   */
  def submit = Action { request =>
    Ok("not implemented")
  }
}