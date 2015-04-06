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
import stringy.EnglishGrammar
import language.postfixOps;

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
      val spaceDelimited = s.split(" +").toSet //split by white spaces
      val commaDelimited = s.split(" ?, ?").toSet
      new Search( spaceDelimited union commaDelimited )
    }

    def unapply(search: Search) = Some(search.tokens.mkString(" "))
  }
  
  def searchForm: Form[Search] = Form(
    mapping("token" -> text)(Search.apply)(Search.unapply))

  /** TODO **/
  def find = Action {
    implicit request =>
      searchForm.bindFromRequest.fold(
        hasErrors => BadRequest(
          views.html.searches("Please narrow down your search.", Nil, username(request))),
        valid => {
          //narrow down by tags
          val projection = PieceKeeper.getWorld
          val results = projection.filter(x => valid.tokens.contains(x._1) ||
            valid.tokens.intersect(x._2.header.tags).size > 0)
          val message = genericSourceMsg.format(results.size, EnglishGrammar.oneOrMore(results.size, "result"), valid.tokens.mkString(","))
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
      val results = PieceKeeper.getWorld.filter(_._2.header.tags.contains(tagName))
      val message = genericTagsMsg.format(results.size,
        EnglishGrammar.oneOrMore(results.size, "result"), tagName.toUpperCase)
      Ok(views.html.searches(message, results, username(request)))
  }

}