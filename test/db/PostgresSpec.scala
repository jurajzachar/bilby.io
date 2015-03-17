package db

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.PostgresDriver
import org.scalatest.Suite
import components.ActiveSlickCake
import java.net.InetAddress

trait PostgresSpec extends Suite {

  private val user = "play"
  private val passwd = "play"
  private val dbname = "play_dev"
  private val driver = "org.postgresql.Driver"

  //db stuff 
  lazy val database = {
      Database.forURL(s"jdbc:postgresql:$dbname?user=$user&password=$passwd", driver = driver)
  }
}

object PostgresSpec {
  //active slick component
  val cake = ActiveSlickCake.getCake(PostgresDriver)
}