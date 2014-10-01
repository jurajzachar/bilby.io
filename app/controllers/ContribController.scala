package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import views._
import models._
import model.Contribution

object ContribController extends Controller {
  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def edit = Action { request =>
    request.session.get("email").map {
      implicit email => Ok(html.contribution.edit( Contribution()))
    }.getOrElse(Ok(html.index("")))
  }
}