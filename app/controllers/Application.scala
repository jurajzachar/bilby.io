package controllers

import play.api.mvc.Action
import play.api.mvc.Controller

object Application extends Controller with Auth.Secured {

  //  def index = Action { request =>
  //    request.session.get("userName").map {
  //      userName => Ok(views.html.index(userName))
  //    }.getOrElse(Ok(views.html.index("")))
  //  }
  //  

  def index = Action {
    request =>
      Ok(views.html.index(username(request)))
  }

    def about = Action {
    request =>
      Ok(views.html.about(username(request)))
  }
}