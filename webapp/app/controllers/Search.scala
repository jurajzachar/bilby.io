package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Controller
import forms.SearchForms.{ navbarForm => searchForm }
import play.api.mvc.Action
import com.google.inject.Inject
import play.api.i18n.MessagesApi
import utils.BackendCore

class Search @Inject() (override val core: BackendCore, val messagesApi: MessagesApi) extends BaseController(core) {
  
  //TODO...
  val genericUserMsg = "Found %d %s for user %s";
  val genericTagsMsg = "Found %d %s for tag '%s'";
  val genericSourceMsg = "Found %d %s for '%s'";

  def find = Action {
    implicit request =>
      searchForm.bindFromRequest.fold(
        hasErrors => BadRequest(
          "Please narrow down your search"),
        valid => {
          val tokens = valid.tokens.map(_.toLowerCase.trim)
          //narrow down by tags
          //val projection = PieceKeeper.getWorld
          //val results = projection.filter {
          //  x =>
          //    tokens.exists(t => x._1.toLowerCase.contains(t)) || //uni-directional user match
          //      tokens.exists(t => (x._2.piece.header.title.toLowerCase).contains(t)) || //uni-directional title match
          //      tokens.exists(t => (x._2.piece.header.tags.exists(tag => tag.toLowerCase.contains(t)))) //bi-directional tag match
          //}
          //val message = genericSourceMsg.format(results.size, EnglishGrammar.oneOrMore(results.size, "result"), valid.tokens.mkString(", "))
          Ok("search not implemented yet.")
        })
  }
}