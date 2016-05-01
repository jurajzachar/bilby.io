package utils

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
import play.api.Configuration
import play.api.Logger
import play.api.libs.ws.WSClient
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.actors.RegistrationServiceImpl
import com.blueskiron.bilby.io.core.actors.AuthenticationServiceImpl
import com.blueskiron.bilby.io.core.module.WSClientModule
import com.blueskiron.bilby.io.core.module.CoreModule
import com.blueskiron.bilby.io.db.PostgresDatabase

@Singleton
class BackendCore @Inject() (
    configuration: Configuration,
    actorSystem: ActorSystem, 
    ws: WSClient, 
    lifecycle: ApplicationLifecycle) {
  
  //parse config 
  val config: Config = configuration.underlying
  Logger.debug(s"underlying config=$config")
  //init core modules
  val wsModule = new WSClientModule(ws)
  val coreModule = CoreModule(actorSystem.dispatchers.lookup("dbio-dispatch"), config)
  //Wrap the injector in a ScalaInjector 
  import net.codingwell.scalaguice.InjectorExtensions._
  val injector = Guice.createInjector(Modules.combine(wsModule, coreModule))
  val authEnv = injector.instance[AuthenticationEnvironment]
  
  //BackedByActor core services
  val regService = RegistrationServiceImpl.startOn(actorSystem, authEnv)
  val authService = AuthenticationServiceImpl.startOn(actorSystem, authEnv)
  
  //inject db instance so that we can shutdown gracefully
  private val appDB = injector.instance[PostgresDatabase]
  lifecycle.addStopHook { () => appDB.closeDatabase() }
}