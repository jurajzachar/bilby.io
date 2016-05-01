package handlers

import play.api.http.DefaultHttpErrorHandler
import com.mohiva.play.silhouette.api.SecuredErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.i18n.{ I18nSupport, MessagesApi, Messages }
import play.api.routing.Router
import scala.concurrent.Future
import javax.inject._
import controllers.routes

//TODO!
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router],
    val messagesApi: MessagesApi) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with SecuredErrorHandler with I18nSupport {

  // 401 - Unauthorized
  override def onNotAuthenticated(request: RequestHeader, messages: Messages): Option[Future[Result]] = Some {
    Future.successful {
      Redirect(routes.Application.index())
    }
  }

  // 403 - Forbidden
  override def onNotAuthorized(request: RequestHeader, messages: Messages): Option[Future[Result]] = Some {
    Future.successful {
      Redirect(routes.Application.index())
    }
  }

  // 404 - page not found error
  override def onNotFound(request: RequestHeader, message: String): Future[Result] = Future.successful {
    NotFound(env.mode match {
      case Mode.Prod => "eek!"
      case _ => "eek!"
    })
  }

  // 500 - internal server error
  override def onProdServerError(request: RequestHeader, exception: UsefulException) = Future.successful {
    InternalServerError("eek!")
  }
}