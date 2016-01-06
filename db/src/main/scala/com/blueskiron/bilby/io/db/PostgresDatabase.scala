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
import com.mohiva.play.silhouette.api.util.ExecutionContextProvider

/**
 * @author juri
 */
@Singleton
class DefaultDatabase @Inject() (override val executionContext: ExecutionContext, override val configPath: String) extends PostgresDatabase

trait PostgresDatabase extends ApplicationDatabase with SlickPgJdbcProfileProvider with ExecutionContextProvider {
  
  import jdbcProfile.api._
  
  def configPath: String
  
  override def setupDb: jdbcProfile.backend.DatabaseDef = {
    val db = Database.forConfig(configPath)
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
  def closeDatabase() = database.close()
  
}
