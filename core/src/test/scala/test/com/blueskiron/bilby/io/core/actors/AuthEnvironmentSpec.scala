package test.com.blueskiron.bilby.io.core.actors

import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.Success

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.auth.module.RegModule
import com.blueskiron.bilby.io.core.testkit.WsTestClientModule
import com.blueskiron.bilby.io.db.DefaultDatabase
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.google.inject.Guice
import com.google.inject.util.Modules

import akka.actor.ActorSystem
import javax.inject.Singleton
import net.codingwell.scalaguice.InjectorExtensions
import play.api.test.WsTestClient

class AuthEnvironmentSpec(testSystem: ActorSystem)
    extends FlatSpec
    with DefaultTestDatabase
    with Matchers 
    with BeforeAndAfterAll {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  //implicit val executionContext = testSystem.dispatchers.lookup("dbio-dispatcher")
  override implicit val executionContext = scala.concurrent.ExecutionContext.global

  val usersWithProfiles = fixtures.usersWithProfiles(fixtures.userProfiles)
  val hasAuthEnv = Promise[AuthenticationEnvironment]()
  

  def this() = this(ActorSystem("AuthEnvironmentSpec"))

  override def beforeAll {
    cleanUp()
    val authModule = RegModule.apply(executionContext, fixtures.dbConfigPath)
    WsTestClient.withClient { client =>
      val wsModule = new WsTestClientModule(client)
      val injector = Guice.createInjector(Modules.combine(wsModule, authModule))
      //Wrap the injector in a ScalaInjector 
      import net.codingwell.scalaguice.InjectorExtensions._
      val defaultDb = injector.instance[DefaultDatabase]
      val authEnv = injector.instance[AuthenticationEnvironment]
      hasAuthEnv.success(authEnv)
    }
  }
  
  "AuthenticationEnvironment" should "be able to correctly bootstrap itself using Guice" in {
      hasAuthEnv.isCompleted shouldBe true
  }
    
  override def afterAll {
    hasAuthEnv.future.andThen{case Success(auth) => auth.userService.shutDown()}
    closeDatabase()
  }

}

