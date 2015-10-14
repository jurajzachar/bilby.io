package test.com.blueskiron.bilby.io.db

import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos._
import com.blueskiron.bilby.io.db.ModelImplicits._
import scala.concurrent.ExecutionContext.Implicits.global
import com.blueskiron.bilby.io.db.Tables
import scala.util.{ Success, Failure }
import org.scalatest.concurrent.{ ScalaFutures, AsyncAssertions, PatienceConfiguration }
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.exceptions.TestFailedException

/**
 * @author juri
 */
class TestDaoFunctions extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  import slick.driver.PostgresDriver.api._

  "ApplicationDatabase" should " be able to perform filter functions on User entity" in {
    fixtures.users.foreach { user => commit(UserRepo.save(userRowsFromUser(user)._1)) }
    val amyQuery = Tables.User.filter { _.firstName === "Foo" }
    val result = query(amyQuery.result)
    result.size shouldBe 7 //hacky magic number
    
  }

  override def afterAll = {
    //clean up users (unique username constraint may fail next test)
    val task = Tables.User.filter { u => u.id === u.id }.delete
    val result = commit(task)
    super.afterAll()
  }
} 