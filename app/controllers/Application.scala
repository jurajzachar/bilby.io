package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc.WebSocket
import actors.Cockatoo

object Application extends Controller with Auth.Secured {

  //  def index = Action { request =>
  //    request.session.get("userName").map {
  //      userName => Ok(views.html.index(userName))
  //    }.getOrElse(Ok(views.html.index("")))
  //  }
  //  

  /**
   *  FIXME: stream live world via websocket using enumeratees...
   */
  def index = Action {
    request =>
      Ok(views.html.index(PieceKeeper.getWorld, username(request)))
  }

  def about = Action {
    request =>
      Ok(views.html.about(username(request)))
  }

  def socket = WebSocket.acceptWithActor[String, String] { request =>
    out =>
      Cockatoo.props(out)
  }
}