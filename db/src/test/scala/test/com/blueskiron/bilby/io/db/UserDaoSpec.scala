package test.com.blueskiron.bilby.io.db

import org.joda.time.LocalDateTime
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.api.model.Role
import com.blueskiron.bilby.io.api.model.UserProfile
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.activeslick.ActiveSlickRepos.UsersRepo
import com.blueskiron.bilby.io.db.dao.UserDao
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.postgresql.slick.Driver
import com.blueskiron.bilby.io.db.codegen.ModelImplicits

/**
 * @author juri
 */
class UserDaoSpec extends FlatSpec with PostgresSuite with UserDao {

  import Driver.api._

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import fixtures._
  val usersWithProfiles = fixtures.usersWithProfiles(fixtures.userProfiles)

  override def beforeAll = {

    //start with a blank slate
    log.info("cleaning up database...")
    cleanUp()

  }

  //lazy val userDao = initUserDao(this)

  def withUserDao[S, T <: PostgresDatabase](testBlock: UserDao[T] => S): S = {
    testBlock(initUserDao(this))
  }

  "UserDao" should "be able to create a new user" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          for (userAndProfile <- usersWithProfiles) {
            val saved = awaitResult(userDao.create(userAndProfile._1, userAndProfile._2.head))
            log.debug(s"saved user: $saved")
          }
          val count = awaitResult(userDao.cake.runAction(UsersRepo.count))
          count shouldBe mockSize
        }
    }

  }

  "UserDao" should "be able to update an exiting user" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          val id = 1L
          val found = awaitResult(userDao.findUser(id))
          val linfo = LoginInfo("native", "1234567890")
          val profile = UserProfile(linfo, None, None, None, None, None, false, new LocalDateTime())
          val updated = awaitResult(userDao.update(found, profile))
          updated.map { user =>
            user.profiles.contains(linfo) shouldBe true
          }
        }
    }
  }

  "UserDao" should "be able to add role to the existing user" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          val id = 1L
          val found = awaitResult(userDao.cake.runAction(UsersRepo.findById(id)))
          val expectedRoles = Set(Role.Admin, Role.User, Role.Customer)
          log.debug(s"found user: $found")
          val currentRoles = found.roles
          currentRoles.size should not be expectedRoles.size
          expectedRoles foreach { r => awaitResult(userDao.addRole(id, r)) }
          val updated = awaitResult(userDao.cake.runAction(UsersRepo.findById(id)))
          updated.roles.map { Role.apply _ }.toSet shouldBe expectedRoles.toSet
        }
    }
  }

  "UserDao" should "be able to find user by username" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          val username = usersWithProfiles.keys.tail.head.username
          val found = awaitResult(userDao.findOptionUser(username))
          log.debug(s"found user: $found")
          found.isDefined shouldBe true
          found.map { _.username shouldBe username }
        }
    }
  }

  "UserDao" should "be able to find user by user profile" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          val linfo = usersWithProfiles.values.tail.head.head.loginInfo
          val found = awaitResult(userDao.findOptionUser(linfo))
          log.debug(s"found user: $found")
          found.isDefined shouldBe true
          //found.map { _.username shouldBe Some("wwellshx") }
        }
    }
  }

  "UserDao" should "be able to find user profile by login info" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          import ModelImplicits.ToModel
          for {
            expectedUser <- usersWithProfiles.keys
            profiles = usersWithProfiles(expectedUser)
            p <- profiles
          } awaitResult(userDao.findUserProfile(p.loginInfo.providerID, p.loginInfo.providerKey)) map {
            up => up shouldBe p
          }
        }
    }
  }

  "UserDao" should "be able to find user profile by email" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          import ModelImplicits.ToModel
          for {
            expectedUser <- usersWithProfiles.keys
            profiles = usersWithProfiles(expectedUser)
            p <- profiles
          } awaitResult(userDao.findUserProfile(p.email)) map {
            up => up shouldBe p
          }
        }
    }
  }

  "UserDao" should "be able to find all user profiles by username" in {
    withUserDao {
      userDao: UserDao[PostgresSuite] =>
        {
          import ModelImplicits.ToModel
          for (expectedUser <- usersWithProfiles.keys) {
            val expectedUsername = expectedUser.username
            val profiles = usersWithProfiles(expectedUser).toSet
            awaitResult(userDao.findAllUserProfiles(expectedUsername)).intersect(profiles) shouldBe profiles
          }
        }
    }
  }
} 