package com.blueskiron.bilby.io.db.testkit

import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.SlickPgJdbcProfileProvider
import scala.concurrent.Await
import scala.concurrent.duration._
import slick.driver.PostgresDriver
import com.blueskiron.bilby.io.db.codegen.Tables
import scala.language.postfixOps

trait DefaultTestDatabase extends PostgresDatabase with SlickPgJdbcProfileProvider {

  import jdbcProfile.api._

  implicit def defaultTimeout = 2 seconds

  override val configPath = "test_db"
  override def setupDb = testDb
  
  override val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  
  lazy val testDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig(configPath)
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
      Tables.Users.filter { u => u.id === u.id }.delete,
      Tables.UserProfiles.filter { up => up.provider === up.provider }.delete
      //...  
    )
    tasks.foreach(statement => Await.result(testDb.run(statement), 1 second))
    //finally reset sequences
    Await.result(testDb.run(resetSequences), 1 second)
  }

}
