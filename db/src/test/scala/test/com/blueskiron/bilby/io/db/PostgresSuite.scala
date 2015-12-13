package test.com.blueskiron.bilby.io.db

import org.scalatest.Suite
import slick.driver.PostgresDriver
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import io.strongtyped.active.slick.JdbcProfileProvider
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.db.SlickPgJdbcProfileProvider
import slick.jdbc.JdbcBackend.DatabaseDef
import com.blueskiron.bilby.io.db.codegen.Tables

/**
 * @author juri
 */
trait PostgresSuite extends DbSuite with SlickPgJdbcProfileProvider {
  self: Suite =>

  import jdbcProfile.api._

  implicit def timeout = 2 seconds

  override def setupDb = testDb
  
  lazy val testDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig("test_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
  def cleanUp() {
    import Tables.profile.api._

    def resetSequences: DBIO[Unit] = {
      DBIO.seq(
        sqlu"""alter sequence users_id_seq restart"""
        //...
      )
    }
    //clean up users, userprofiles and visitors (unique username constraint may fail next test)
    val tasks = List(
      Tables.Users.filter { u => u.id === u.id }.delete
      //...  
    )
    tasks.foreach(statement => Await.result(testDb.run(statement), 1 second))
    //finally reset sequences
    Await.result(testDb.run(resetSequences), 1 second)
  }

}

