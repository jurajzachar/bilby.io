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

class ComponentSpec extends PlaySpec
  with OneAppPerSuite
  with UserComponent
  with MustMatchers {

  val log = LoggerFactory.getLogger(this.getClass)

  // Override app if you need a FakeApplication with other than
  // default parameters.
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
              log.info("Dropping database schema inside the fake app...")
              dropSchema
            } catch {
              case t: Throwable => println(t.getMessage) // TODO: handle error
            }
          }
          log.info("Fake application started!")
        }
      }))

  val UserComponent = InitUserComponent(PostgresSpec.cake)

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
    "return a seq of registered user" in new MockCase {
      val registeredUsers = for {
        v <- visitors.slice(0, 10)
        uprofile <- userProfiles.slice(0, 10)
        u <- users.slice(0, 10)
      } yield {
        log.info(s"Signing up user: ${u.userName}")
        UserComponent.signUpNewUser(u, uprofile, v)
      }
      registeredUsers.map(_.isDefined).size must be(MOCK_SIZE);
    }
  }
}