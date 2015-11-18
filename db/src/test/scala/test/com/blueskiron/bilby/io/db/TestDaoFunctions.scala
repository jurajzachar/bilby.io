package test.com.blueskiron.bilby.io.db

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos.UserRepo
import com.blueskiron.bilby.io.db.Tables
import com.blueskiron.bilby.io.db.dao.UserDao
import scala.concurrent.{ Await, ExecutionContext }
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.api.model.{ User, UserProfile, Visitor }
import com.blueskiron.bilby.io.api.UserService.UserNameAlreadyTaken
import com.blueskiron.bilby.io.api.UserService.EmailAddressAleadyRegistered
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.blueskiron.bilby.io.db.ActiveSlickRepos._
import scala.util.Random

/**
 * @author juri
 */
class TestDaoFunctions extends FlatSpec with PostgresSuite {

  import TestDatabase._

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import slick.driver.PostgresDriver.api._

  override def beforeAll {
    import jdbcProfile.api._
    import com.blueskiron.bilby.io.db.ar.ModelImplicits.ToDataRow._
    for {
      data <- fixtures.compiledUsers
    } {
      val user = data._1
      val account = data._2
      val userProfile = data._3
      val visitor = data._4
      lazy val savedEntities = (
        commit(AccountRepo.save(account)),
        commit(UserprofileRepo.save(userProfile)),
        commit(VisitorRepo.save(visitor)))
      val extendedUser = rowFromUserNameAndForeignKeys(user.userName, savedEntities._1.id, Some(savedEntities._2.id), Some(savedEntities._3.id), None)
      log.info("saving user: {}", extendedUser)
      val savedUser = commit(UserRepo.save(extendedUser))
    }
  }

  "ApplicationDatabase" should " be able to perform filter functions on User entity" in {
    val randomUsername = randomUser.userName
    log.debug("picked random username to query the database: {}", randomUsername)
    val userNameQuery = Tables.User.filter { _.userName === randomUsername }
    val result = query(userNameQuery.result)
    result.size shouldBe 1
  }

  "UserDao" should " be able to compile and execute queries on User entity" in new UserDao {
    val _randomUser = randomUser
    val randomEmail = randomUser.account.email
    log.debug("picked random email to query the database: {}", randomEmail)
    //confirm email is in json users
    val sqlAction = userDao.compiledUserFromEmail(randomEmail).result.headOption
    val q = query(sqlAction)
    q.isDefined shouldBe true
    //retrieve a full user (inc. Account, UserProfile, Visitor)
    val fullUser = q flatMap { userRow => query(userDao.compiledFullUserFromId(userRow.id).result.headOption) }
    log.debug("full user: {}", fullUser)
    val withErrorMsg = "failed to retrieve a complete user account details: " + fullUser
    fullUser match {
      case Some(x) => x match {
        case (userRow, accountRow, Some(userprofileRow), Some(visitor)) => randomEmail shouldBe accountRow.email
        case _ => fail(withErrorMsg)
      }
      case _ => fail(withErrorMsg)
    }
  }

  "UserDao" should " be able to retrieve user based on provided user name or email address" in new TestUserDao {
    val _randomUser = randomUser
    val email = _randomUser.account.email
    val userName = _randomUser.userName
    val user1 = Await.result(userDao.userFromEitherUserNameOrEmail(email), timeout)
    val user2 = Await.result(userDao.userFromEitherUserNameOrEmail(userName), timeout)
    user1.isDefined shouldBe true
    user2.isDefined shouldBe true
    user1 shouldEqual user2
  }

  //FIX-ME! 
  ignore should " be able to save a new userprofile or retrieve an existing one" in new TestUserDao {
    val userProf = fixtures.userProfiles.head
    val saved = Await.result(userDao.handleUserProfile(userProf), timeout)
    (saved.firstName == userProf.firstName && saved.lastName == userProf.lastName && 
        saved.country.equals(userProf.country) && saved.placeOfRes.equals(userProf.placeOfRes) &&
        saved.age == userProf.age) shouldBe true
    val unchanged = Await.result(userDao.handleUserProfile(userProf), timeout)
    //unchanged shouldEqual saved
  }

  //FIX-ME! 
  ignore should " be able to save a new visitor and update a returning one" in new TestUserDao {
    val visitor = fixtures.visitors.head
    val savedVisitor = userDao.handleVisitor(visitor)
    val updatedVisitor = savedVisitor.flatMap { _ => userDao.handleVisitor(visitor) }
    val readSavedVisitor = Await.result(savedVisitor, timeout)

    //check it's the same visitor
    visitor.host shouldEqual readSavedVisitor.host

    //check timestamp is updated
    //visitor.timestamp == readSavedVisitor.timestamp shouldBe true

    //now check updates
    val readUpdatedVisitor = Await.result(updatedVisitor, timeout)
    readSavedVisitor.id shouldEqual readUpdatedVisitor.id
    visitor.host shouldEqual readUpdatedVisitor.host
    readSavedVisitor.timestamp != readUpdatedVisitor.timestamp shouldBe true
  }

  "UserDao" should " be able to sign up a new user and reject duplicate registration" in new TestUserDao {
    //explicitly call cleanup to prevent unique key constraints
    TestDatabase.cleanUp
    val extras = (fixtures.accounts.head, fixtures.userProfiles.head, fixtures.visitors.head)
    val user = User.create(None, fixtures.users.head.userName, extras._1, Some(extras._2), Some(extras._3))
    log.info("new user attempting to sign up: {}", user)
    val outcome1 = Await.result(userDao.signupUser(user), timeout)
    outcome1 fold (
      userRow => userRow.userName shouldEqual user.userName,
      rejected => fail("New user failed to sign up because: " + rejected))

    //repeated signup should fail
    val outcome2 = Await.result(userDao.signupUser(user), timeout)
    outcome2 fold (
      userRow => fail("User must never sign up twice using the same username or email address"),
      rejected => (rejected.isInstanceOf[UserNameAlreadyTaken] ||
        rejected.isInstanceOf[EmailAddressAleadyRegistered]) shouldBe true)
  }

  private def randomUser = {
    fixtures.users(new Random(System.currentTimeMillis()).nextInt(fixtures.users.size))
  }

  override def afterAll = {
    TestDatabase.cleanUp
    super.afterAll()
  }

  //helper trait to inject UserDao into test cases
  sealed trait TestUserDao extends UserDao {
    val testAppDb = new TestApplicationDatabase(database)
    override lazy val userDao = initWithApplicationDatabase(testAppDb)
    val timeout: FiniteDuration = 5.seconds
  }
} 