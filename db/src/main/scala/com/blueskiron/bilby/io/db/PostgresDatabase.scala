package com.blueskiron.bilby.io.db

import slick.driver.PostgresDriver
import slick.driver
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import io.strongtyped.active.slick.JdbcProfileProvider
/**
 * @author juri
 */
object PostgresDatabase extends PostgresDatabase

trait PostgresDatabase extends ApplicationDatabase with JdbcProfileProvider.PostgresProfileProvider {
  
  import jdbcProfile.api.Database
  
  override def setupDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig("prod_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
}