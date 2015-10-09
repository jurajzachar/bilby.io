package test.com.blueskiron.bilby.io.db

import org.scalatest.Suite
import slick.driver.{ PostgresDriver, JdbcDriver }
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import io.strongtyped.active.slick.JdbcProfileProvider

/**
 * @author juri
 */
trait PostgresSuite extends DbSuite with JdbcProfileProvider.PostgresProfileProvider {
  self: Suite =>

  import jdbcProfile.api._

  def timeout = 2 seconds

  override def setupDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig("test_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
}