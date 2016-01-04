package test.com.blueskiron.bilby.io.core.actors

import scala.concurrent.Promise
import scala.language.postfixOps
import scala.util.Success
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.core.auth.module.AuthModule
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.testkit.WsTestClientModule
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import javax.inject.Singleton
import net.codingwell.scalaguice.InjectorExtensions
import play.api.test.WsTestClient
import scala.concurrent.Await
import com.blueskiron.bilby.io.api.model.User
import scala.util.Failure
import com.blueskiron.bilby.io.api.UserService.Response
import com.blueskiron.bilby.io.db.DefaultDatabase
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase
import org.scalatest.FlatSpec

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
    val authModule = AuthModule.apply(executionContext, fixtures.dbConfigPath)
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
  
//    
//    "be able to sign up new users" in {
//      
//      val authEnv = Await.result(hasAuthEnv.future, defaultTimeout)
//      val work = for (e <- usersWithProfiles) yield { 
//        log.debug("signing up user: {}", e)
//        Await.result(authEnv.userService.create(e._1, e._2.head), defaultTimeout)
//      }
//      work.filter(_.isLeft).size shouldBe usersWithProfiles.size
//     }
//   }
    
  
  override def afterAll {
    hasAuthEnv.future.andThen{case Success(auth) => auth.userService.shutDown()}
    closeDatabase()
  }

}

