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

/**
 * @author juri
 */
class TestDaoFunctions extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import slick.driver.PostgresDriver.api._

  override def beforeAll {
    import com.blueskiron.bilby.io.db.ar.ModelImplicits._
    fixtures.users.foreach { user => commit(UserRepo.save(user)) }
  }

  "ApplicationDatabase" should " be able to perform filter functions on User entity" in {
    val amyQuery = Tables.User.filter { _.userName === "jrichardsg6" }
    val result = query(amyQuery.result)
    result.size shouldBe 1 //hacky magic number
  }

  "UserDao" should " be able to compile and execute queries on User entity" in new UserDao {
    val userName = "whawkins2x"
    //confirm email is in json users
    fixtures.users.filter { _.userName.equals(userName) }.size shouldBe 1
    val sqlAction = userDao.userFromEmail(userName).result.headOption
    val q = query(sqlAction)
    q.isDefined shouldBe true
    q.map { _.userName shouldEqual userName }
  }

  "UserDao" should " be able to retrieve user based on provided user name or email address" in new TestUserDao {
    val email = "lwilliamsonnw@csmonitor.com"
    val userName = "lwilliamsonnw"
    val user1 = Await.result(userDao.userFromEitherUserNameOrEmail(email), timeout)
    val user2 = Await.result(userDao.userFromEitherUserNameOrEmail(userName), timeout)
    user1.isDefined shouldBe true
    user2.isDefined shouldBe true
    user1 shouldEqual user2
  }

  "UserDao" should " be able to save a new userprofile or retrieve an existing one" in new TestUserDao {
    val userProf = fixtures.userProfiles.head
    val saved = Await.result(userDao.handleUserProfile(userProf), timeout)
    (saved.country.equals(userProf.country) &&
      saved.placeOfRes.equals(userProf.placeOfRes) &&
      saved.age == userProf.age) shouldBe true
    val unchanged = Await.result(userDao.handleUserProfile(userProf), timeout)
    unchanged shouldEqual saved
  }

  "UserDao" should " be able to save a new visitor and update a returning one" in new TestUserDao {
    val visitor = fixtures.visitors.head
    val savedVisitor = userDao.handleVisitor(visitor)
    val updatedVisitor = savedVisitor.flatMap { _ => userDao.handleVisitor(visitor) }
    val readSavedVisitor = Await.result(savedVisitor, timeout)

    //check it's the same visitor
    visitor.host shouldEqual readSavedVisitor.host

    //check timestamp is updated
    
    visitor.timestamp == readSavedVisitor.timestamp shouldBe true

    //now check updates
    val readUpdatedVisitor = Await.result(updatedVisitor, timeout)
    readSavedVisitor.id shouldEqual readUpdatedVisitor.id
    visitor.host shouldEqual readUpdatedVisitor.host
    readSavedVisitor.timestamp != readUpdatedVisitor.timestamp shouldBe true
  }

  "UserDao" should " be able to sign up a new user and reject duplicate registration" in new TestUserDao {
    //explicitly call cleanup to prevent unique key constraints
    cleanUp
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
          rejected.isInstanceOf[EmailAddressAleadyRegistered]) shouldBe true
    )
  }

  private def cleanUp() = {
    //clean up users, userprofiles and visitors (unique username constraint may fail next test)
    val tasks = List(
      Tables.User.filter { u => u.id === u.id }.delete,
      Tables.Userprofile.filter { v => v.id === v.id }.delete,
      Tables.Visitor.filter { v => v.id === v.id }.delete)
    tasks.foreach(commit(_))
  }

  override def afterAll = {
    cleanUp
    super.afterAll()
  }

  //helper trait to inject UserDao into test cases
  sealed trait TestUserDao extends UserDao {
    val testAppDb = new TestApplicationDatabase(database)
    override lazy val userDao = initWithApplicationDatabase(testAppDb)
    val timeout: FiniteDuration = 5.seconds
  }
} 