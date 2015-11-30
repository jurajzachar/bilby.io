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
import com.blueskiron.bilby.io.api.model.JsonConversions._
import com.blueskiron.bilby.io.api.model.User
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.dao.UserDao
import com.blueskiron.bilby.io.api.UserService.Signup
import com.blueskiron.bilby.io.api.UserService.SignupOutcome
import play.api.libs.json._
import com.blueskiron.bilby.io.core.actors.AuthService
import com.blueskiron.bilby.io.api.UserService.Authenticate
import com.blueskiron.bilby.io.core.actors.SignupService

class CoreSpec(testSystem: ActorSystem)
    extends TestKit(testSystem)
    with DefaultTimeout with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  import com.blueskiron.bilby.io.mock.MockBilbyFixtures._

  val log = LoggerFactory.getLogger(this.getClass)

  val defaultTimeout = 10 seconds

  def this() = this(ActorSystem("CoreSpec"))

  lazy val authService = AuthService.startOn(testSystem) //4 workers
  lazy val signupService = SignupService.startOn(testSystem) //4 workers

  lazy val users = for {
    userData <- compiledUsers
  } yield {
    User.create(None, userData._1.userName, userData._2, Some(userData._3), Some(userData._4))
  }

  override def beforeAll = {
    log.info("Test data sample: {} units", users.size)
    log.info("Signup worker intialized: {}", signupService.path)
    log.info("AuthService intialized: {}", authService.path)
  }

  override def afterAll {
    import com.blueskiron.bilby.io.db.testkit.TestDatabase._
    TestKit.shutdownActorSystem(testSystem)
    cleanUp
  }

  "Signup service" must {
    "send back a SignupOutcome with a signed-up user" in {
      users foreach { user =>
        signupService ! Signup(user)
        expectMsgPF(defaultTimeout) {
          case SignupOutcome(x) => x match {
            case Left(y) => log.debug("Signup successful for user: {}", Json.prettyPrint(Json.toJson(y)))
            case _       => fail("failed to signup user: " + user)
          }
          case x: Any => fail("unexpected actor communication detected: " + x)
        }
      }
    }
  }

  "Authentication service" must {
    "authenticate valid users" in {
      users foreach { user =>
        val request = Authenticate((user.userName, user.account.password))
        authService ! request
        expectMsgPF(defaultTimeout) {
          case Some(user) => log.debug("Success auth response with {}", request)
          case None       => fail("a valid user was not authenticated: " + user)
        }
      }
    }
  }
}

