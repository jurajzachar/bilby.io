import org.squeryl.SessionFactory
import play.api.{GlobalSettings,Application}
import org.squeryl.Session
import play.api.db.DB

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    SessionFactory.concreteFactory = Some( () =>
      Session.create(DB.getConnection("bilby_db", true)(app), new org.squeryl.adapters.PostgreSqlAdapter) )
  }

}