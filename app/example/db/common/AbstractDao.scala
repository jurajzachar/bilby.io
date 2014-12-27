package example.db.common

import play.api.db.DB
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._

abstract class AbstractDao {
  
  val database = Database.forDataSource(DB.getDataSource())

}
