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
  private val remote = "jupiter"

  //db stuff 
  lazy val database = {
    if (InetAddress.getByName(remote).isReachable(3000))
      Database.forURL(s"jdbc:postgresql://$remote/$dbname?user=$user&password=$passwd", driver = driver)
    else
      Database.forURL(s"jdbc:postgresql:$dbname?user=$user&password=$passwd", driver = driver)
  }
}

object PostgresSpec {
  //active slick component
  val cake = ActiveSlickCake.getCake(PostgresDriver)
}