package test.com.blueskiron.bilby.io.core

import scala.concurrent.Await
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.mock.MockitoSugar
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.core.actors.AuthenticationServiceImpl
import com.blueskiron.bilby.io.core.actors.RegistrationServiceImpl
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.module.CoreModule
import com.blueskiron.bilby.io.core.module.WSClientModule
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.service.UserService
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import javax.inject.Singleton
import net.codingwell.scalaguice.InjectorExtensions
import play.api.libs.ws.WSClient
import com.typesafe.config.ConfigFactory

class AuthenticationServicesSpec(testSystem: ActorSystem) extends TestKit(testSystem)
    with DefaultTestDatabase
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AuthServiceSpec"))

  val log = LoggerFactory.getLogger(this.getClass)
  val fixtures = MockBilbyFixtures
  override implicit val executionContext = testSystem.dispatchers.lookup("dbio-dispatch")
  val coreModule = CoreModule.apply(executionContext, ConfigFactory.defaultApplication())
  val wsModule = new WSClientModule(mock[WSClient])
  val injector = Guice.createInjector(Modules.combine(wsModule, coreModule))
  //Wrap the injector in a ScalaInjector 
  import net.codingwell.scalaguice.InjectorExtensions._
  val defaultDb = injector.instance[PostgresDatabase]
  val authEnv = injector.instance[AuthenticationEnvironment]
  val userService = injector.instance[UserService[PostgresDatabase]]
  val regService = RegistrationServiceImpl.startOn(testSystem, authEnv)
  val authService = AuthenticationServiceImpl.startOn(testSystem, authEnv)
  
  override def beforeAll {
    cleanUp()
  }

  "RegistrationService" must {
    "deliver a valid RegistrationActor with workers" in {
      val rounds = 10
      for (i <- 1 to rounds + 1) regService ! "Hello workers!"
      expectMsgPF(defaultTimeout) {
        case msg: Any => log.info("received={}", msg)
      }
      receiveN(rounds, defaultTimeout)
    }
  }

  "RegistrationService" must {
    "sign up a valid user" in {
      userService.count.map(_ shouldBe 0)
      val expectedSize = CoreTestData.registrationRequests.size
      CoreTestData.registrationRequests foreach { regService ! _ }
      val collected = receiveWhile(defaultTimeout*10, defaultTimeout, expectedSize) { //max, min, total nr. messages
        case res: AuthenticatorResult =>
          log.info("received={}", res); res //OK 
        case msg: Any => log.error("received={}", msg)
      }
      collected.size shouldBe expectedSize
    }
  }

  "AuthenticationService" must {
    "authenticate a registered user" in {
      val expectedSize = CoreTestData.authenticationRequests.size
      CoreTestData.authenticationRequests foreach { authService ! _ }
      val collected = receiveWhile(defaultTimeout*10, defaultTimeout, expectedSize) { //max, min, total nr. messages
        case Some(user) =>
          log.info("received authenticated user ={}", user); //OK 
        case None => log.info("authentication for user {?} failed")
        
        case msg: Any => log.error("unexpected response={}", msg)
      }
      collected.size shouldBe expectedSize
    }
  }

  override def afterAll {
    Await.ready(closeDatabase(), defaultTimeout)
  }

}