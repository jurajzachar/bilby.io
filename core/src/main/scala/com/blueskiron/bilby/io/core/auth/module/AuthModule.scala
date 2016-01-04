package com.blueskiron.bilby.io.core.auth.module

import com.google.inject.{AbstractModule, Inject, Provides}
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import com.blueskiron.bilby.io.core.auth.{AuthenticationEnvironment, AuthenticationActor}
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorSystem, ActorRef}
import com.blueskiron.bilby.io.db.service.DbModule

object AuthModule {
  def apply(ex: ExecutionContext, dbConfigPath: String) = 
    com.google.inject.util.Modules.combine(new ConfigModule, new AuthModule, new DbModule(ex, dbConfigPath))
}
class AuthModule extends AbstractModule with ScalaModule {
  
  override def configure = {
    bind[AuthenticationEnvironment]
  }
 
}