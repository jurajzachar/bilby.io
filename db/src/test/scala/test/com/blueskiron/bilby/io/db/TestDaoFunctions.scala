package test.com.blueskiron.bilby.io.db

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos.UserRepo
import com.blueskiron.bilby.io.db.Tables
import com.blueskiron.bilby.io.db.ar.ModelImplicits.userRowFromUser
import com.blueskiron.bilby.io.db.dao.UserDao
import scala.concurrent.{ Await, ExecutionContext }
import com.blueskiron.bilby.io.db.ApplicationDatabase
import com.blueskiron.bilby.io.model.UserProfile


/**
 * @author juri
 */
class TestDaoFunctions extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import slick.driver.PostgresDriver.api._
  
  override def beforeAll {
    fixtures.users.foreach { user => commit(UserRepo.save(userRowFromUser(user))) }
  }

  "ApplicationDatabase" should " be able to perform filter functions on User entity" in {
    val amyQuery = Tables.User.filter { _.firstName === "Amy" }
    val result = query(amyQuery.result)
    result.size shouldBe 7 //hacky magic number
  }

  "UserDao" should " be able to compile and execute queries on User entity" in new UserDao {
    val email = "lwilliamsonnw@csmonitor.com"
    //confirm email is in json users
    fixtures.users.filter { _.email.equals(email) }.size shouldBe 1
    val sqlAction = userDao.userFromEmail(email).result.headOption
    val q = query(sqlAction)
    q.isDefined shouldBe true
    q.map { _.email shouldEqual email }
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
        saved.placeOfResidence.equals(userProf.placeOfResidence) && 
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

  override def afterAll = {
    //clean up users, userprofiles and visitors (unique username constraint may fail next test)
    val tasks = List(
        Tables.User.filter { u => u.id === u.id }.delete,
        Tables.Userprofile.filter { v => v.id === v.id }.delete,
        Tables.Visitor.filter { v => v.id === v.id }.delete)
    tasks.foreach(commit(_))
    super.afterAll()
  }
  
  //helper trait to inject UserDao into test cases
  sealed trait TestUserDao extends UserDao {
    val testAppDb = new TestApplicationDatabase(database)
    override lazy val userDao = initWithApplicationDatabase(testAppDb)
    val timeout: FiniteDuration = 5.seconds
  }
} 