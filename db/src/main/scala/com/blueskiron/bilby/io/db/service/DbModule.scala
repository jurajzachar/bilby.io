package com.blueskiron.bilby.io.db.service

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.blueskiron.bilby.io.db.PostgresDatabase
import scala.concurrent.ExecutionContext
import javax.inject.{ Inject, Singleton }

@Singleton
case class DefaultDatabase(override val configPath: String)(override implicit val executionContext: ExecutionContext) extends PostgresDatabase

/**
 * Bootstrap database services by injecting desired execution context and config path.
 * @author juri
 *
 */
class DbModule(ec: ExecutionContext, configPath: String) extends AbstractModule with ScalaModule {

  lazy val database = DefaultDatabase(configPath)(ec)

  override def configure {
    bind[ExecutionContext].toInstance(ec)
    bind[PostgresDatabase].toInstance(database) //PostgresDatabase
    bind[PasswordInfoService[PostgresDatabase]]
    bind[SessionInfoService[PostgresDatabase]]
    bind[UserService[PostgresDatabase]]
  }

}