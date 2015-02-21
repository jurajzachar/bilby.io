package components

import play.api.db.slick.Config
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.driver.JdbcDriver
import org.slf4j.LoggerFactory
import models._
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import scala.language.postfixOps
import java.nio.file.StandardOpenOption

class ActiveSlickCake(override val jdbcDriver: JdbcDriver)
  extends ActiveSlick with Schema with SchemaExtensions {

  import jdbcDriver.simple._
  
  val evolutionsFile = "./conf/evolutions/default/0.sql"
  val logger = LoggerFactory.getLogger(this.getClass)

  def createSchema(implicit session: Session) = {
    ddl.create
    val dump = ("# --- !Ups\n" + ddl.createStatements.map(_ + ";").mkString("\n")).getBytes
    Files.write(Paths.get(evolutionsFile), dump, StandardOpenOption.CREATE)
  }

  def dropSchema(implicit session: Session) = {
    ddl.drop
    val dump = ("\n# --- !Downs\n" + ddl.dropStatements.map(_ + ";").mkString("\n")).getBytes
    Files.write(Paths.get(evolutionsFile), dump, StandardOpenOption.APPEND)
  }
}

object ActiveSlickCake {
  lazy val cake = getCake()
  def getCake(driver: scala.slick.driver.JdbcDriver = Config.driver): ActiveSlickCake = new ActiveSlickCake(driver)
}
