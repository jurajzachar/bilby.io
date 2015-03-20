package controllers

import components.PieceBindings
import components.PieceComponent
import models.Piece
import models.PieceFormInfo
import models.User
import play.api.Logger.logger
import play.api.Play.current
import play.api.data.Form
import play.api.db.slick.DB
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session
import play.utils.UriEncoding

object PieceKeeper extends Controller
  with PieceBindings
  with Auth.Secured
  with Authorized {

  /* this will go away once i figure out how to use localized messages */
  val draftSavedMsg = "Your draft has been saved."
  val publishMsg = "Your draft has been published."
  val unpublishMsg = "Your post has been taken down."
  val pieceDeletedMsg = "Your post has been deleted."
  val notAllowedMsg = "This action is not allowed"

  trait PieceAction {}
  case class Save(id: Long, author: User) extends PieceAction
  case class Publish(id: Long, author: User, hasFormData: Boolean) extends PieceAction
  case class Unpublish(id: Long, author: User) extends PieceAction

  /**
   * Read only rendering of pieces.
   * TODO: setup sharing and privacy traits
   * @return
   */
  def render(uri: String) = Action {
    request =>
      //not implemented
      Ok(views.html.index(username(request)))
  }

  /**
   * pre-authenticated GET request for pieces overview
   */
  def list = withUser { user =>
    implicit request =>
      Ok(views.html.piece.list(user.username, dal.listAll(user.id.get)))

  }

  /**
   * pre-authenticated GET request for editor
   */
  def create = withUser { user =>
    implicit request =>
      Ok(views.html.piece.edit(0, form.fill(dal.draft), username(request)))
  }

  /**
   * Secured editing of pieces.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  def edit(id: Long) = withAuthorOf(id) { user =>
    implicit request =>
      val formInfo = dal.findByPieceId(id, user.id.get).header
      Ok(views.html.piece.edit(id, form.fill(formInfo), username(request)))
  }

  /**
   * Secured draft submission of edited pieces.
   * At this point post is saved but not yet posted.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  private def save(action: Save)(implicit request: Request[_]) = {
    form.bindFromRequest.fold(
      errors => {
        logger.debug(s"Save request on piece id: ${action.id} has error: $errors")
        BadRequest(views.html.piece.edit(action.id, errors, Some(action.author.username)))
      },
      pieceFormInfo => {
        logger.debug("Saving a new piece: " + pieceFormInfo.title)
        val saved = dal.save({ if (action.id == 0) None else Some(action.id) }, action.author.id.get, pieceFormInfo)
        Redirect(routes.PieceKeeper.edit(saved.id.get)).flashing("success" -> draftSavedMsg)
      })
  }

  /**
   *  Secured deletion of contributions.
   */
  def delete(id: Long, user: User)(implicit request: Request[_]) = {
    dal.delete(id)
    Redirect(routes.PieceKeeper.list).flashing("success" -> pieceDeletedMsg)
  }
  /**
   * Secured submission of edited pieces.
   * Submitted piece is posted.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  //TODO: fixme! this is very ugly!
  def publish(action: PieceAction)(implicit request: Request[_]) = {

    def resolveSavedStatus(id: Long, author: User, pieceFormInfo: PieceFormInfo) = {
      dal.save({
        if (id == 0) None else Some(id)
      }, author.id.get, pieceFormInfo).id.get
    }

    //TODO: update user activity ...
    action match {
      case Publish(id, author, hasPieceForm) => {
        val pieceFormInfo = {
          if (hasPieceForm) form.bindFromRequest
          else {
            DB.withSession {
              implicit session: Session =>
                form.fill(dal.cake.Pieces.findById(id).header)
            }
          }
        }
        pieceFormInfo.fold(
          errors => {
            BadRequest(views.html.piece.edit(id, errors, Some(author.username)))
          },
          pieceFormInfo => {
            dal.publish(id)

            if (hasPieceForm)
              Redirect(routes.PieceKeeper.edit(resolveSavedStatus(id, author, pieceFormInfo))).flashing("success" -> publishMsg)
            else Redirect(routes.PieceKeeper.list).flashing("success" -> publishMsg)

          })
      }
      case Unpublish(id, author) => {
        dal.unpublish(id)
        Redirect(routes.PieceKeeper.list).flashing("success" -> unpublishMsg)
      }
      case _ => BadRequest(notAllowedMsg)
    }

  }
  def editorAction(id: Long) = withAuthorOf(id) { user =>
    implicit request =>
      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("save")    => save(Save(id, user))(request)
        case Some("publish") => publish(Publish(id, user, true))(request)
        case _               => BadRequest(notAllowedMsg)
      }
  }

  def overviewAction(id: Long) = withAuthorOf(id) { user =>
    implicit request =>
      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("publish")  => publish(Publish(id, user, false))(request)
        case Some("unpublish") => publish(Unpublish(id, user))(request)
        case Some("edit")     => Redirect(routes.PieceKeeper.edit(id))
        case Some("delete")   => delete(id, user)(request)
        case _                => BadRequest(notAllowedMsg)
      }
  }
}
trait Authorized extends Auth.Secured {
  dal: PieceComponent =>

  /**
   * Check if the connected user is a owner of this task.
   */
  def withAuthorOf(pieceId: Long)(f: => User => Request[AnyContent] => Result) = withUser {
    user =>
      request =>
        if (pieceId == 0 || dal.isOwner(pieceId, user.id.get)) {
          f(user)(request)
        } else {
          Results.Forbidden
        }
  }
}

