package test.com.blueskiron.bilby.io.core

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import org.slf4j.LoggerFactory
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import akka.testkit.DefaultTimeout
import akka.actor.ActorSystem
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import play.api.test.WsTestClient
import scala.concurrent.Promise
import akka.actor.ActorRef
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.blueskiron.bilby.io.core.auth.module.CoreModule
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.blueskiron.bilby.io.core.actors.RegistrationServiceImpl
import play.api.libs.ws.WSClient
import org.scalatest.mock.MockitoSugar
import org.mockito._
import com.blueskiron.bilby.io.db.service.UserService
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.blueskiron.bilby.io.db.service.DefaultDatabase
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.Json
import com.blueskiron.bilby.io.api.model.JsonConversions._

class RegistrationServiceSpec(testSystem: ActorSystem)
    extends TestKit(testSystem)
    with DefaultTestDatabase
    with DefaultTimeout
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("RegServiceSpec"))
  val log = LoggerFactory.getLogger(this.getClass)
  val fixtures = MockBilbyFixtures
  override implicit val executionContext = testSystem.dispatchers.lookup("dbio-dispatch")
  val coreModule = CoreModule.apply(executionContext, fixtures.dbConfigPath)
  val wsModule = new WSTestClientModule(mock[WSClient])
  val injector = Guice.createInjector(Modules.combine(wsModule, coreModule))
  //Wrap the injector in a ScalaInjector 
  import net.codingwell.scalaguice.InjectorExtensions._
  val defaultDb = injector.instance[PostgresDatabase]
  val authEnv = injector.instance[AuthenticationEnvironment]
  val userService = injector.instance[UserService[PostgresDatabase]]
  val regService = RegistrationServiceImpl.startOn(testSystem, authEnv)

  override val defaultTimeout = 40 * 5 seconds //more time allowed to bootstrap the whole akka system

  override def beforeAll {
    cleanUp()
  }

  "RegistrationService" must {
    "deliver a valid RegistrationActor with workers" in {
      val rounds = 10
      for (i <- 1 to rounds+1) regService ! "Hello workers!"
      expectMsgPF(defaultTimeout) {
        case msg: Any => log.info("received={}", msg)
      }
      receiveN(rounds, defaultTimeout)
    }
  }

  "RegistrationService" must {
    "sign up a valid user" in {
      userService.count.map(_ shouldBe 0)
      val regRequests = fixtures.usersWithProfiles().map(e => TestUtils.buildFakeRegistrationRequest(e._1, e._2.head))
      regRequests foreach { regService ! _ }
      val collected = receiveWhile(defaultTimeout, defaultTimeout/10, fixtures.mockSize) { //max, min, total nr. messages
        case res: AuthenticatorResult =>
          log.info("received={}", res); res //OK 
        case msg: Any                 => log.error("received={}", msg)
      }
      collected.size shouldBe fixtures.mockSize
    }
  }
  
  override def afterAll {
    Await.ready(closeDatabase(), defaultTimeout) 
  }

}