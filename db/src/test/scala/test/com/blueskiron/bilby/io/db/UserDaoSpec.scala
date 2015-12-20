package test.com.blueskiron.bilby.io.db

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.db.codegen.Tables
import com.blueskiron.bilby.io.db.dao.UserDao
import scala.concurrent.{ Await, ExecutionContext }
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.UserService.UserNameAlreadyTaken
import com.blueskiron.bilby.io.api.UserService.EmailAddressAleadyRegistered
import scala.util.Random
import org.scalatest.DoNotDiscover
import com.blueskiron.postgresql.slick.Driver
import com.blueskiron.bilby.io.api.model.User
import org.joda.time.LocalDateTime
import com.blueskiron.bilby.io.api.model.Role
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.ExecutionContext.Implicits.global
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import com.blueskiron.bilby.io.mock.MockBilbyFixtures

/**
 * @author juri
 */
class UserDaoSpec extends FlatSpec with PostgresSuite with UserDao {

  import Driver.api._

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import fixtures._

  override def beforeAll = {

    //start with a blank slate
    log.info("cleaning up database...")
    cleanUp()

  }

  override lazy val userDao = initWithApplicationDatabase(this)

  "UserDao" should "be able to create a new user" in {
    for (mockUser <- users) {
      val saved = awaitResult(userDao.cake.commit(UsersRepo.save(ModelImplicits.ToDataRow.rowFromUser(mockUser))))
      log.debug(s"saved user: $saved")
    }

    val count = awaitResult(userDao.cake.runAction(UsersRepo.count))
    count shouldBe mockSize

  }

  "UserDao" should "be able to add role to the existing user" in {
    val id = 1L
    val found = awaitResult(userDao.cake.runAction(UsersRepo.findById(id)))
    val existingRoles = found.roles.map(Role(_))
    log.debug(s"found user: $found")
    val _newRole = Role("user")
    existingRoles.contains(_newRole) shouldBe false
    val updated = awaitResult(userDao.addRole(id, _newRole))
    updated.roles.toSet shouldBe (_newRole :: existingRoles).toSet
  }

  "UserDao" should "be able to find user by username" in {
    val username = "howensl7"
    val found = awaitResult(userDao.findUserByUserName(username))
    log.debug(s"found user: $found")
    found.isDefined shouldBe true
    found.map { _.username shouldBe Some(username) }
  }

  "UserDao" should "be able to find user by user profile" in {
    val linfo = LoginInfo("zoiw", "fcwhve")
    val found = awaitResult(userDao.findUserByProfile(linfo))
    log.debug(s"found user: $found")
    found.isDefined shouldBe true
    found.map { _.username shouldBe Some("drogersq1") }
  }

  private def generateFakeUsers(size: Int) = {
    for (i <- 1 to size) yield User(
      None,
      Some(randomString(5)),
      List(LoginInfo(randomString(4), randomString(6)), LoginInfo(randomString(4), randomString(8))),
      Set(Role.Admin, Role.User),
      true,
      LocalDateTime.now)
  }

  private def randomString(length: Int) = java.util.UUID.randomUUID().toString().substring(0, length + 1)

} 