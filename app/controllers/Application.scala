package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.cache.Cached
import play.api.Play.current

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
  def index =  Action {
    request =>
      val tups = for(x <- PieceKeeper.getWorld; y <- x._2) yield (x._1, y)
      val popularAndRecentFirst = tups.sortBy(x => (x._2.rating, x._2.published.get)).reverse
      Ok(views.html.index(popularAndRecentFirst, username(request)))
  }

  def about = Action {
    request =>
      Ok(views.html.about(username(request)))
  }
}