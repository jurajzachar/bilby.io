package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import components.PieceComponent
import play.api.mvc.AnyContent
import models.Piece
import components.PieceBindings
import controllers.Auth.CSFRHelper

object PieceKeeper extends Controller with PieceBindings with Auth.Secured {

  /**
   * Read only rendering of pieces.
   * TODO: setup sharing and privacy traits
   * @return
   */
  def render(uri: String) = Action {
    request =>
      //not implemented
      Ok(views.html.index(username(request).getOrElse("")))
  }

  def draft = CSFRHelper.withToken { token => 
    implicit request =>{
      Ok(views.html.piece.edit(form,""))
 
    }
 }
  

  /**
   * Secured editing of pieces.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def edit(id: Long) = withUser { user => implicit request =>
      val formInfo = dal.findByPieceId(Some(id), id).header
      Ok(views.html.piece.edit(form.fill(formInfo), user.username))
  }

  /**
   * Secured draft submission of edited pieces.
   * At this point post is saved but not yet posted.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def save(id: Long) = Action {
    request =>
      Ok(views.html.index(username(request).getOrElse("")))
  }

  /**
   * Secured submission of edited pieces.
   * Submitted piece is posted.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def post(id: Long) = Action {
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