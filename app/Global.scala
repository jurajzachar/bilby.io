import play.api._
import play.api.Play.current
import components.ActiveSlickCake.cake._
import play.filters.csrf._
import play.api.mvc.WithFilters

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    //TODO generate diag report  
  }
 
}