package com.blueskiron.bilby.io.db.service

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.DefaultDatabase
import scala.concurrent.ExecutionContext
import javax.inject.Inject

/**
 * Bootstrap database services by injecting desired execution context and config path.
 * @author juri
 *
 */
class DbModule(ex: ExecutionContext, configPath: String) extends AbstractModule with ScalaModule {
  
  lazy val database = new DefaultDatabase(ex, configPath)
  
  override def configure {
     bind[ExecutionContext].toInstance(ex)
     bind[PostgresDatabase].toInstance(database) //PostgresDatabase
     bind[PasswordInfoService[DefaultDatabase]]
  	 bind[SessionInfoService[DefaultDatabase]]
  	 bind[UserService[DefaultDatabase]]
  }
  
}