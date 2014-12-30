package components

import play.api.db.slick.Config
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.driver.JdbcDriver

class ActiveSlickCake(override val jdbcDriver: JdbcDriver)
  extends ActiveSlick with Schema with SchemaExtensions {

  import jdbcDriver.simple._

  def createSchema(implicit session: Session) = {
    Players.ddl.create
    Visitors.ddl.create
  }

  def dropSchema(implicit session: Session) = {
    Players.ddl.drop
    Visitors.ddl.drop
  }
}

object ActiveSlickCake {
  val cake = new ActiveSlickCake(Config.driver)
}
