package example.db.common

import java.sql.DriverManager
import scala.collection.JavaConverters._
import scala.slick.codegen.SourceCodeGenerator
import org.h2.tools.Server

object Util {
  /** A helper function to unload all JDBC drivers so we don't leak memory */
  def unloadDrivers {
    DriverManager.getDrivers.asScala.foreach { d =>
      DriverManager.deregisterDriver(d)
    }
  }
}

object SlickGenerator extends App {

  val slickDriver = "scala.slick.driver.PostgresDriver"
  val jdbcDriver = "org.postgresql.Driver"
  val url = "jdbc:postgresql://localhost:5432/play_dev"
  val outputFolder = "app"
  val pkg = "models.db.common"

  SourceCodeGenerator.main(
    Array(slickDriver, jdbcDriver, url, outputFolder, pkg))
  
}
