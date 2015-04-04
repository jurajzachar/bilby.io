package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import components.UserComponent
import stringy.EnglishGrammar

object SearchEngine extends Controller with Auth.Secured {

  val searchUserMsg = "Found %d %s for user %s";
  val searchTagsMsg = "Found %d %s for tag '%s'";

  /** TODO **/
  def find(token: String) = Action {
    val spaceDelimited = token.split(" +") //split by white spaces
    val commaDelimited = token.split(" ?, ?")
    val tokens = (spaceDelimited.toList union commaDelimited.toList).filter(!_.contains(","))
    //narrow down by tags
    
    //narrow down by users
    Ok
  }

  def findUser(userId: String) = Action {
    request =>
      val results = PieceKeeper.getWorld.filter(_._1.equals(userId))
      val message = searchUserMsg.format(results.size,
        EnglishGrammar.oneOrMore(results.size, "result"), userId.toUpperCase)
      Ok(views.html.searches(message, results, username(request)))
  }

  def findByTag(tagName: String) = Action {
    request =>
      val results = PieceKeeper.getWorld.filter(_._2.header.tags.contains(tagName))
      val message = searchTagsMsg.format(results.size,
        EnglishGrammar.oneOrMore(results.size, "result"), tagName.toUpperCase)
      Ok(views.html.searches(message, results, username(request)))
  }

}