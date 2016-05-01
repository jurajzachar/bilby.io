package test.com.blueskiron.bilby.io.core

import scala.concurrent.Await
import scala.concurrent.Promise
import scala.util.Success

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.slf4j.LoggerFactory

import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.module.CoreModule
import com.blueskiron.bilby.io.core.module.WSClientModule
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import javax.inject.Singleton
import net.codingwell.scalaguice.InjectorExtensions
import play.api.libs.ws.WSClient

class AuthEnvironmentSpec(testSystem: ActorSystem)
    extends FlatSpec
    with DefaultTestDatabase
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  val usersWithProfiles = fixtures.usersWithProfiles(fixtures.userProfiles)
  val hasAuthEnv = Promise[AuthenticationEnvironment]()
  override implicit val executionContext = scala.concurrent.ExecutionContext.global
  def this() = this(ActorSystem("AuthEnvironmentSpec"))

  override def beforeAll {
    cleanUp()
    val authModule = CoreModule.apply(executionContext, ConfigFactory.defaultApplication())
    val wsModule = new WSClientModule(mock[WSClient])
    val injector = Guice.createInjector(Modules.combine(wsModule, authModule))
    //Wrap the injector in a ScalaInjector 
    import net.codingwell.scalaguice.InjectorExtensions._
    val defaultDb = injector.instance[PostgresDatabase]
    val authEnv = injector.instance[AuthenticationEnvironment]
    hasAuthEnv.success(authEnv)
  }

  "AuthenticationEnvironment" should "be able to correctly bootstrap itself using Guice" in {
    hasAuthEnv.isCompleted shouldBe true
  }

  override def afterAll {
    hasAuthEnv.future.andThen { case Success(auth) => Await.ready(closeDatabase(), defaultTimeout) }
  }
}