package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.slf4j.{ Logger, LoggerFactory }

object Application extends Controller with Auth.Secured {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  //  def index = Action { request =>
  //    request.session.get("userName").map {
  //      userName => Ok(views.html.index(userName))
  //    }.getOrElse(Ok(views.html.index("")))
  //  }
  //  

  def index = Action {
    request =>
      Ok(views.html.index(username(request).getOrElse("")))
  }

    def about = Action {
    request =>
      Ok(views.html.about(username(request).getOrElse("")))
  }
}