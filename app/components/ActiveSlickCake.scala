package components

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import scala.language.postfixOps
import scala.slick.driver.JdbcDriver

import io.strongtyped.active.slick.ActiveSlick
import play.api.db.slick.Config

class ActiveSlickCake(override val jdbcDriver: JdbcDriver)
  extends ActiveSlick with Schema with SchemaExtensions {

  import jdbcDriver.simple._
  
  val evolutionsFile = "./conf/evolutions/default/1.sql"

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
