package com.blueskiron.bilby.io.core.module

import com.google.inject.{AbstractModule, Inject, Provides}
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorSystem, ActorRef}
import com.blueskiron.bilby.io.db.service.DbModule
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.typesafe.config.Config

object CoreModule {
  
  def apply(ec: ExecutionContext, config: Config) = {
    com.google.inject.util.Modules.combine(new DbModule(ec, config), new CoreModule(config))
  }
}

class CoreModule(val config: Config) extends AbstractModule with ScalaModule {
  
  override def configure = {
    bind[Config].toInstance(config)
    bind[AuthenticationEnvironment]
  }
 
}