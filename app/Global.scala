import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.NotFound
import play.api.mvc.WithFilters
import play.filters.csrf._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    //TODO generate diag report  
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Future {
      NotFound(
        views.html.notfound(request.path))
    }
  }

}