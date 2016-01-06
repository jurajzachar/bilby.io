package com.blueskiron.bilby.io.core.auth.module

import com.google.inject.{AbstractModule, Inject, Provides}
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorSystem, ActorRef}
import com.blueskiron.bilby.io.db.service.DbModule
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment

object RegModule {
  def apply(ex: ExecutionContext, dbConfigPath: String) = 
    com.google.inject.util.Modules.combine(new ConfigModule, new DbModule(ex, dbConfigPath), new RegModule)
}
class RegModule extends AbstractModule with ScalaModule {
  
  override def configure = {
    bind[AuthenticationEnvironment]
  }
 
}