package controllers

import controllers.Auth.CSFRHelper
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import utils.EnglishGrammar
import language.postfixOps
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import components.JsonConversions._
import play.api.Play.current
import play.api.cache.Cache

object SearchEngine extends Controller with Auth.Secured {

  val genericUserMsg = "Found %d %s for user %s";
  val genericTagsMsg = "Found %d %s for tag '%s'";
  val genericSourceMsg = "Found %d %s for '%s'";

  class Search(val tokens: Set[String])

  object Search {

    /**
     * Extract unique set of search tokens
     * @param s
     * @return
     */
    def apply(s: String): Search = {
      //val spaceDelimited = s.split(" +").toSet //split by white spaces
      //val commaDelimited = s.split(" ?, ?").toSet
      //new Search(spaceDelimited union commaDelimited)
    	new Search(Set(s))
    }

    def unapply(search: Search) = Some(search.tokens.mkString(" "))
  }

  def searchForm: Form[Search] = Form(
    mapping("searchToken" -> text)(Search.apply)(Search.unapply))

  /** TODO **/
  def find = Action {
    implicit request =>
      searchForm.bindFromRequest.fold(
        hasErrors => BadRequest(
          views.html.searches("Please narrow down your search.", Nil, username(request))),
        valid => {
          val tokens = valid.tokens.map(_.toLowerCase.trim)
          //narrow down by tags
          val projection = PieceKeeper.getWorld
          val results = projection.filter {
            x =>
              tokens.exists(t => x._1.toLowerCase.contains(t)) || //uni-directional user match
                tokens.exists(t => (x._2.piece.header.title.toLowerCase).contains(t)) || //uni-directional title match
                tokens.exists(t => (x._2.piece.header.tags.exists(tag => tag.toLowerCase.contains(t)))) //bi-directional tag match
          }
          val message = genericSourceMsg.format(results.size, EnglishGrammar.oneOrMore(results.size, "result"), valid.tokens.mkString(", "))
          Ok(views.html.searches(message, results, username(request)))
        })
  }

  def findUser(userId: String) = Action {
    request =>
      val results = PieceKeeper.getWorld.filter(_._1.equals(userId))
      val message = genericUserMsg.format(results.size,
        EnglishGrammar.oneOrMore(results.size, "result"), userId.toUpperCase)
      Ok(views.html.searches(message, results, username(request)))
  }

  def findByTag(tagName: String) = Action {
    request =>
      val results = PieceKeeper.getWorld.filter(_._2.piece.header.tags.contains(tagName))
      val message = genericTagsMsg.format(results.size,
        EnglishGrammar.oneOrMore(results.size, "result"), tagName.toUpperCase)
      Ok(views.html.searches(message, results, username(request)))
  }

  def cachedDatasets = {
    Cache.getOrElse("tta", 60) {
      Map(
        "users" -> PieceKeeper.getWorld.map(_._1).toSet,
        "titles" -> PieceKeeper.getWorld.map(_._2.piece.header.title).toSet,
        "tags" -> PieceKeeper.getWorld.map(_._2.piece.header.tags).flatten.toSet)
    }
  }

  def prefetchDataset(dataset: String) = Action {
   Ok(Json.toJson(cachedDatasets(dataset).map(x => Map("value" -> x))))
  }

  def typeAhead(dataset: String, query: Option[String]) = Action {
    query match {
      case Some(q) => Ok(Json.toJson(cachedDatasets(dataset).filter(_.contains(q)).map(x => Map("value" -> x))))
      case None    => Ok("[]")
    }

  }
}