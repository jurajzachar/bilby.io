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

trait PostgresSpec extends Suite with BeforeAndAfterAll {

  private val dbname = "play_dev"
  private val driver = "org.postgresql.Driver"

  //db
  lazy val database = Database.forURL(s"jdbc:postgresql:$dbname", driver = driver)

}

object PostgresSpec {
  //active slick component
  val cake = ActiveSlickCake.getCake(PostgresDriver)
}