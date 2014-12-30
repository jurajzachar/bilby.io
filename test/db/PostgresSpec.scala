package db

import org.scalatestplus.play.PlaySpec
import org.scalatest.BeforeAndAfter
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import Database.dynamicSession
import org.slf4j.LoggerFactory
import org.scalatest.Suite
import org.scalatest.BeforeAndAfterAll

trait PostgresSpec extends Suite with BeforeAndAfterAll {
  private val dbname = "play_dev"
  private val driver = "org.postgresql.Driver"

  lazy val database = Database.forURL(s"jdbc:postgresql:$dbname", driver = driver)
  
}