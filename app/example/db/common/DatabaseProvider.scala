package example.db.common

import play.api.db.DB
import play.api.Play.current

trait DatabaseProvider {
  lazy val database = DB.getDataSource()
}
