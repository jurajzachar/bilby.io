package test.com.blueskiron.bilby.io.core.actors

import akka.actor.{ Actor, ActorSystem, Props }
import akka.testkit.{ TestKit, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._
import scala.language.postfixOps
import com.blueskiron.bilby.io.core.actors.SignupWorker
import com.typesafe.config.ConfigFactory
import akka.testkit.DefaultTimeout
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.UserService.SignupRequest
import test.com.blueskiron.bilby.io.core.MockCoreFixtures
import org.slf4j.LoggerFactory

class CoreSpec(testSystem: ActorSystem) 
  extends TestKit(testSystem)
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val log = LoggerFactory.getLogger(this.getClass)
  
  def this() = this(ActorSystem("CoreSpec"))
  
  override def afterAll {
    TestKit.shutdownActorSystem(testSystem)
  }
  
  "A Signup service" must {
    import MockCoreFixtures._
    val t = (users.head, userProfiles.head, visitors.head)
    val user = User.userWithProfileAndVisitor(t._1, Some(t._2), Some(t._3))
    "send back a SignupOutcome with a signed-up user" in {
      val signupService = testSystem.actorOf(Props[SignupWorker])
      signupService ! SignupRequest(user)
      expectMsgPF(1500 millis) {
        case response => log.info("SignupService response: {}", response)
      }
    }
  }
}

