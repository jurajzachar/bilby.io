package com.blueskiron.bilby.io.db

import slick.driver
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import io.strongtyped.active.slick.JdbcProfileProvider

/**
 * @author juri
 */
object PostgresDatabase extends PostgresDatabase

trait PostgresDatabase extends ApplicationDatabase with SlickPgJdbcProfileProvider {
  
  import jdbcProfile.api._
  
  override def setupDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig("app_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
}