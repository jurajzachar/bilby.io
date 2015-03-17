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
  val draftSavedMsg = "Your Draft has been saved."
  val publishMsg = "Your draft has been published. You can find it here:\n\n"
  val pieceDeletedMsg = "Your contribution has been deleted."

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
  private def save(id: Long, user: User)(implicit request: Request[_]) = {
    form.bindFromRequest.fold(
      errors => {
        logger.debug(s"Save request on piece id: $id has error: $errors")
        BadRequest(views.html.piece.edit(id, errors, Some(user.username)))
      },
      pieceFormInfo => {
        logger.debug("Saving a new piece: " + pieceFormInfo.title)
        val saved = dal.save({ if (id == 0) None else Some(id) }, user.id.get, pieceFormInfo)
        Redirect(routes.PieceKeeper.edit(saved.id.get)).flashing("success" -> draftSavedMsg)
      })
  }

  /**
   * Secured submission of edited pieces.
   * Submitted piece is posted.
   * TODO: setup collaboration and and edit permissions.
   * @return
   */
  //TODO: fixme! this is very ugly!
  def publish(id: Long, user: User, hasFormData: Boolean)(implicit request: Request[_]) = {

    def processForm(form: Form[PieceFormInfo]) = {
      form.fold(
        errors => {
          BadRequest(views.html.piece.edit(id, errors, Some(user.username)))
        },
        pieceFormInfo => {
          logger.debug("Publishing a new piece: " + pieceFormInfo.title)
          val saved = dal.save({ if (id == 0) None else Some(id) }, user.id.get, pieceFormInfo)
          var uri = s"""${user.username}/${saved.id.get}/${UriEncoding.encodePathSegment(saved.header.title, "UTF-8")}"""
          if (hasFormData) {
            Redirect(routes.PieceKeeper.edit(saved.id.get)).flashing("success" -> { publishMsg + request.host + routes.PieceKeeper.render(uri).toString })

          } else {
            Redirect(routes.PieceKeeper.list).flashing("success" -> { publishMsg + request.host + routes.PieceKeeper.render(uri).toString })

          }
        })
    }

    hasFormData match {
      case true => processForm(form.bindFromRequest)
      case false => {
        DB.withSession {
          implicit session: Session =>
            val data = dal.cake.Pieces.findById(id).header
            processForm(form.fill(data))
        }
      }
    }
  }

  /**
   *  Secured deletion of contributions.
   */
  def delete(id: Long, user: User)(implicit request: Request[_]) = {
    dal.delete(id)
    Redirect(routes.PieceKeeper.list).flashing("success" -> pieceDeletedMsg)
  }

  def editorAction(id: Long) = withAuthorOf(id) { user =>
    implicit request =>
      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("save")    => save(id, user)(request)
        case Some("publish") => publish(id, user, true)(request)
        case _               => BadRequest("This Action is Not Allowed!")
      }
  }

  def overviewAction(id: Long) = withAuthorOf(id) { user =>
    implicit request =>
      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("publish")   => publish(id, user, false)(request)
        case Some("unpublish") => Ok //to-do
        case Some("edit")      => Redirect(routes.PieceKeeper.edit(id))
        case Some("delete")    => delete(id, user)(request)
        case _                 => BadRequest("This Action is Not Allowed!")
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

