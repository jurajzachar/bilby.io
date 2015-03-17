package db

import org.scalatest.BeforeAndAfter
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import org.slf4j.LoggerFactory
import PostgresSpec.cake.createSchema
import PostgresSpec.cake.dropSchema
import components.UserComponent
import play.api.Application
import play.api.GlobalSettings
import play.api.Play
import play.api.db.slick.DB
import play.api.test.FakeApplication
import org.scalatestplus.play.OneAppPerSuite
import scala.language.postfixOps
import models._

class ComponentSpec extends PlaySpec
  with OneAppPerSuite
  with UserComponent
  with MustMatchers
  with MockCase {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map(
        "ehcacheplugin" -> "disabled",
        "evolutionplugin" -> "disabled"),
      withGlobal = Some(new GlobalSettings() {
        override def onStart(app: Application) {
          play.api.db.slick.DB(app).withTransaction { implicit session =>
            try {
              log.info("Creating database schema inside the fake app...")
              createSchema
            } catch {
              case t: Throwable => println(t.getMessage) // TODO: handle error
            }
          }
          log.info("Fake application started!")
        }
        override def onStop(app: Application) {
          play.api.db.slick.DB(app).withTransaction { implicit session =>
            try {
              //log.info("Dropping database schema inside the fake app...")
              dropSchema
            } catch {
              case t: Throwable => println(t.getMessage) // TODO: handle error
            }
          }
          log.info("Fake application started!")
        }
      }))

  val UserComponent = initComponent(PostgresSpec.cake)

  "The OneAppPerSuite trait" must {
    "provide a FakeApplication" in {
      log.info("Fake App root path: {}", app.path.getAbsolutePath)
      app.configuration.getString("ehcacheplugin") mustBe Some("disabled")
    }
    "start the FakeApplication" in {
      Play.maybeApplication mustBe Some(app)
    }
  }

  "calling signUpNewUser on mock users" must {
    "return a seq of registered user" in {
      val registeredUsers = for {
        wrapped: User#Wrapped <- (visitors, userProfiles, users).zipped.toList
      } yield {
        log.info(s"Signing up user:\n${wrapped}")
        UserComponent.signUpNewUser(wrapped)
      }
      log.info("number of registration hits:" + registeredUsers.size)
      registeredUsers.map(_.right).size must be(MOCK_SIZE)
    }
  }

  "authenticating a user" must {
    //signup a user
    "return an authenticated user if username and password match" in {
     val username = "dgeorge8"
      val secret = "nPo9BTClsg"
      val candidateId = UserComponent.getIdByUniqueKey(username)
      candidateId match {
        case Some(id) => //ignore
        case None => UserComponent.signUpNewUser((
          Visitor(None, System.currentTimeMillis(), None), 
          UserProfile(None, None, None, None), 
          users.find(_.username.equals(username)).get))
      }
      val candidate = UserComponent.authenticate(username, secret).get
      candidateId == candidate.id mustBe true
    }

    "return an authenticated user if email address and password match" in {
      val email = """dgeorge8@sakura.ne.jp"""
      val secret = "nPo9BTClsg"
      val candidateId = UserComponent.getIdByUniqueKey(email)
      candidateId match {
        case Some(id) => //ignore
        case None => UserComponent.signUpNewUser((
          Visitor(None, System.currentTimeMillis(), None), 
          UserProfile(None, None, None, None), 
          users.find(_.email.equals(email)).get))
      }
      val candidate = UserComponent.authenticate(email, secret).get
      candidateId == candidate.id mustBe true
    }

    "return nothing if password does not match" in {
      //authenticate unsuccessfully (wrong password)
      val candidate = UserComponent.authenticate("dgeorge8", "NPo9BTClSg")
      candidate mustBe None
    }

    "return nothing if username does not match" in {
      //authenticate unsuccessfully (wrong password)
      val candidate = UserComponent.authenticate("dgeorge0", "nPo9BTClsg")
      candidate mustBe None
    }
  }
}