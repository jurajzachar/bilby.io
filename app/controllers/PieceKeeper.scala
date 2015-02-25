package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import components.PieceComponent
import play.api.mvc.AnyContent
import models.Piece

object PieceKeeper extends Controller with PieceComponent with Auth.Secured {

  /**
   * Read only rendering of pieces.
   * TODO: setup sharing and privacy traits
   * @return
   */
  def render(id: Long) = Action {
    request =>
      Ok(views.html.index(username(request).getOrElse("")))
  }

  /**
   * Secured editing of pieces.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def edit(id: Long) = withUser { user =>
    implicit request =>
      Ok(views.html.piece.edit(dal.findByPieceId({ if (id == 0) None else Some(id) })
          (user.id.get))(user.username))
  }

  /**
   * Secured submission of edited pieces.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def save = Action {
    request =>
      Ok(views.html.index(username(request).getOrElse("")))
  }

  trait Authorized {

    /**
     * Check if the connected user is a owner of this task.
     */
    def withAuthorOf(pieceId: Long)(f: => String => Request[AnyContent] => Result) = withUser { user =>
      request =>
        if (dal.isOwner(pieceId, user.id.getOrElse(-1))) {
          f(user.username)(request)
        } else {
          Results.Forbidden
        }
    }

    /**
     * Check if the connected user is an authorized co-author of this piece.
     */
    //    def IsMemberOf(project: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user =>
    //      request =>
    //        if (Project.isMember(project, user)) {
    //          f(user)(request)
    //        } else {
    //          Results.Forbidden
    //        }
    //    }

  }
}