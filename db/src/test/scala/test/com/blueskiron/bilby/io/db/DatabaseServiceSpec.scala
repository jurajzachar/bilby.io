package test.com.blueskiron.bilby.io.db

import org.scalatest.FlatSpec
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import org.slf4j.LoggerFactory
import scala.concurrent.Promise
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.UserProfile
import com.google.inject.Guice
import com.blueskiron.bilby.io.db.service.DbModule
import com.blueskiron.bilby.io.db.service.PasswordInfoService
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.service.SessionInfoService
import com.blueskiron.bilby.io.db.service.UserService
import scala.concurrent.ExecutionContext.Implicits.global
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders

class DatabaseServiceSpec extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  val injector = Guice.createInjector(new DbModule(executionContext, configPath))
  //Wrap the injector in a ScalaInjector for even more rich scala magic
  import net.codingwell.scalaguice.InjectorExtensions._
  val passwordInfoService = injector.instance[PasswordInfoService[PostgresDatabase]]
  val sessionInfoService = injector.instance[SessionInfoService[PostgresDatabase]]
  val userService = injector.instance[UserService[PostgresDatabase]]
  val usersWithProfiles = fixtures.usersWithProfiles(fixtures.userProfiles)
  override def beforeAll = {
    //start with a blank slate
    log.info("cleaning up database...")
    cleanUp()
  }

  "DbModule" must "provide valid singleton instances of all database services" in {
    passwordInfoService should not be null
    sessionInfoService should not be null
    userService should not be null

    //check for singletons
    passwordInfoService.hashCode shouldBe injector.instance[PasswordInfoService[PostgresDatabase]].hashCode
    sessionInfoService.hashCode shouldBe injector.instance[SessionInfoService[PostgresDatabase]].hashCode
    userService.hashCode shouldBe injector.instance[UserService[PostgresDatabase]].hashCode
  }
  
  //user service
  "UserService" must "be able to sign up new users and refuse duplicate signup requests" in {
    def workA = for (entry <- usersWithProfiles) yield {
      awaitResult(userService.create(entry._1, entry._2.head))
    }

    //1st cycle should succeed
    workA.foreach {
      _ match {
        case Left(user)    => //ok
        case Right(reject) => fail("Failed to sign up a new user: " + reject)
      }
    }

    //2nd cycle should fail
    workA.foreach {
      _ match {
        case Left(user)    => if (user.profiles.filter(_.providerID == SupportedAuthProviders.NATIVE.id).isEmpty) fail("Native provider cannot be signed up twice!") //else ok
        case Right(reject) => log.debug("User sign up failed due to: " + reject)
      }
    }
    
    awaitResult(userService.count) shouldBe usersWithProfiles.size
  }

  "UserService" must "be able to update existing user profiles" in {
    val expectedCount = awaitResult(userService.count)
    log.debug("existing user count: {}", expectedCount) 
    val workB = for (entry <- fixtures.usersWithProfiles(fixtures.moreUserProfiles)) yield {
      
      awaitResult(userService.handle(entry._1, entry._2.head, false))
    }
    workB.size shouldBe expectedCount
  }
}