package db

import org.scalatestplus.play.PlaySpec
import org.scalatest.BeforeAndAfter
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.{ JdbcProfile, PostgresDriver }
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import Database.dynamicSession
import org.slf4j.LoggerFactory
import org.scalatest.Suite
import org.scalatest.BeforeAndAfterAll
import components.ActiveSlickCake
import java.net.InetAddress

trait PostgresSpec extends Suite with BeforeAndAfterAll {

  private val dbname = "play_dev"
  private val driver = "org.postgresql.Driver"
  private val remote = "jupiter"

  //db stuff 
  lazy val database = {

    if (InetAddress.getByName(remote).isReachable(3000))
      Database.forURL(s"jdbc:postgresql://$remote/$dbname", driver = driver)
    else   
      Database.forURL(s"jdbc:postgresql:$dbname", driver = driver)
  }
}

object PostgresSpec {
  //active slick component
  val cake = ActiveSlickCake.getCake(PostgresDriver)
}