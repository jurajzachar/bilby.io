package com.blueskiron.bilby.io.db

import slick.driver
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import io.strongtyped.active.slick.JdbcProfileProvider
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import javax.inject.Inject
import com.typesafe.config.Config

/**
 * @author juri
 */
trait PostgresDatabase extends ApplicationDatabase with SlickPgJdbcProfileProvider {
  
  import jdbcProfile.api._
  
  implicit def executionContext: ExecutionContext
    
  def configPath: String
  
  override def setupDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig(configPath)
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
  def closeDatabase() = {
    database.shutdown
  }
  
}
