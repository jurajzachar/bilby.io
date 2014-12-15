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
  private val dbname = getClass.getSimpleName.toLowerCase
  private val driver = "org.postgresql.Driver"

  private val postgres = Database.forURL("jdbc:postgresql:postgres", driver = driver)
  postgres withDynSession {
    Q.updateNA(s"DROP DATABASE IF EXISTS $dbname").execute
    Q.updateNA(s"CREATE DATABASE $dbname").execute
  }

  override def afterAll() {
    postgres withDynSession Q.updateNA(s"DROP DATABASE $dbname").execute
  }

  val database = Database.forURL(s"jdbc:postgresql:$dbname", driver = driver)
}