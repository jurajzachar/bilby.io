package controllers

import models._
import utils.silhouette._
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.{ MessagesApi, Messages, Lang }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import javax.inject.Inject
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment

//class Application @Inject() (val env: AuthenticationEnvironment, val messagesApi: MessagesApi) extends SilhouetteController {
class Application @Inject() (val messagesApi: MessagesApi) extends BaseController {
  
  def index = Action { implicit request =>
    Ok(views.html.index(Nil, None))
  }

//  def selectLang(lang: String) = Action { implicit request =>
//    Logger.logger.debug("Change user lang to : " + lang)
//    request.headers.get(REFERER).map { referer =>
//      Redirect(referer).withLang(Lang(lang))
//    }.getOrElse {
//      Redirect(routes.Application.index).withLang(Lang(lang))
//    }
//  }

}