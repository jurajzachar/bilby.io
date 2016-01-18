package com.blueskiron.bilby.io.db.testkit

import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.SlickPgJdbcProfileProvider
import scala.concurrent.Await
import scala.concurrent.duration._
import slick.driver.PostgresDriver
import com.blueskiron.bilby.io.db.codegen.Tables
import scala.language.postfixOps
import com.typesafe.config.ConfigFactory

trait DefaultTestDatabase extends PostgresDatabase with SlickPgJdbcProfileProvider {
  
  import jdbcProfile.api._

  override val configPath = "test_db"
  
  private lazy val config = ConfigFactory.load()
  
  implicit def defaultTimeout = FiniteDuration(config.getInt("test_db.defaultTimeout"), "seconds")
  
  def cleanUp() {
    import Tables.profile.api._

    def resetSequences: DBIO[Unit] = {
      DBIO.seq(
        sqlu"""alter sequence users_id_seq restart""",
        sqlu"""alter sequence assets_id_seq restart"""
        //...
      )
    }
    //clean up users, userprofiles and visitors (unique username constraint may fail next test)
    val tasks = List(
      Tables.Users.filter { u => u.id === u.id }.delete,
      Tables.UserProfiles.filter { up => up.provider === up.provider }.delete
      //...  
    )
    tasks.foreach(statement => Await.result(database.run(statement), defaultTimeout))
    //finally reset sequences
    Await.result(database.run(resetSequences), defaultTimeout)
  }

}
