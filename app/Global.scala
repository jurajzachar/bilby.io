import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.NotFound
import play.api.mvc.WithFilters
import play.filters.csrf._
import components.ActiveSlickCake
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session
import play.libs.Akka
import akka.actor.Props
import actors.Yallara
import actors.Yallara.CacheWorld
import scala.concurrent.duration._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    //TODO generate diag report  
    //DB.withSession {
    //    implicit session: Session =>
    //      ActiveSlickCake.cake.createSchema
    //      ActiveSlickCake.cake.dropSchema
    val cacheBuilder = Akka.system.actorOf(Props(new Yallara(60)), name = "yallara")
    Akka.system.scheduler.schedule(0.microsecond, 61.seconds, cacheBuilder, CacheWorld)
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Future {
      NotFound(
        views.html.notfound(request.path))
    }
  }

}