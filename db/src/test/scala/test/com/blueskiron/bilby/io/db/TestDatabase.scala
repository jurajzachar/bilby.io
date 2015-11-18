package test.com.blueskiron.bilby.io.db

import slick.jdbc.JdbcBackend.Database
import com.blueskiron.bilby.io.db.Tables
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
/**
 * @author juri
 *
 */
object TestDatabase {
  
  lazy val testDatabase: Database = {
    val db = Database.forConfig("test_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
  def cleanUp() {
    import Tables.profile.api._
    
    def resetSequences: DBIO[Unit] = {
      DBIO.seq(
      sqlu"""alter sequence user_id_seq restart""",
      sqlu"""alter sequence account_id_seq restart""",
      sqlu"""alter sequence userprofile_id_seq restart""",
      sqlu"""alter sequence visitor_id_seq restart""",
      sqlu"""alter sequence asset_id_seq restart"""
      )
    }
    //clean up users, userprofiles and visitors (unique username constraint may fail next test)
    val tasks = List(
      Tables.User.filter { u => u.id === u.id }.delete,
      Tables.Userprofile.filter { v => v.id === v.id }.delete,
      Tables.Visitor.filter { v => v.id === v.id }.delete,
      Tables.Account.filter { a => a.id === a.id }.delete)
    tasks.foreach(statement => Await.result(testDatabase.run(statement), 1 second))
    //finally reset sequences
    Await.result(testDatabase.run(resetSequences), 1 second)
  }
}