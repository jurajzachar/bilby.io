package components

import play.api.db.slick.Config
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.driver.JdbcDriver
import org.slf4j.LoggerFactory

class ActiveSlickCake(override val jdbcDriver: JdbcDriver)
  extends ActiveSlick with Schema with SchemaExtensions {

  import jdbcDriver.simple._
  
  val logger = LoggerFactory.getLogger(this.getClass)
  def createSchema(implicit session: Session) = {
    ddl.createStatements.foreach(println(_))
    ddl.create
  }

  def dropSchema(implicit session: Session) = {
    ddl.dropStatements.foreach(println(_))
    ddl.drop
  }
}

object ActiveSlickCake {
  lazy val cake = getCake()
  def getCake(driver: scala.slick.driver.JdbcDriver = Config.driver): ActiveSlickCake = new ActiveSlickCake(driver)
}
