package test.com.blueskiron.bilby.io.core.actors

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
import com.blueskiron.bilby.io.db.DefaultDatabase
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.google.inject.Guice
import com.blueskiron.bilby.io.core.testkit.WsTestClientModule
import com.google.inject.util.Modules
import com.blueskiron.bilby.io.core.auth.module.RegModule
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.blueskiron.bilby.io.core.auth.RegistrationServiceImpl

class RegServiceSpec(testSystem: ActorSystem)
    extends TestKit(testSystem)
    with DefaultTestDatabase
    with DefaultTimeout 
    with ImplicitSender
    with WordSpecLike 
    with Matchers 
    with BeforeAndAfterAll {
  
  def this() = this(ActorSystem("RegServiceSpec"))
  val log = LoggerFactory.getLogger(this.getClass)
  val fixtures = MockBilbyFixtures
  val promiseActor = Promise[ActorRef]() //promise fulfilled in beforeAll
  override val defaultTimeout = 20 seconds //more time allowed to bootstrap the whole akka system
   
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
      promiseActor.success(RegistrationServiceImpl.startOn(testSystem, authEnv))
    }
  }
  
  "AuthenticationService" must {
    "deliver a valid AuthenticationActor ref" in {
      promiseActor.isCompleted shouldBe true
      val authService = Await.result(promiseActor.future, defaultTimeout)
      for(i <- 1 to 1000) authService ! "Hello workers!"
      expectMsgPF(defaultTimeout) {
          case msg: Any => log.info("received={}", msg)
        }
    }
  }
}