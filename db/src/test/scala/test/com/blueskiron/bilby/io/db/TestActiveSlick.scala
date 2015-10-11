package test.com.blueskiron.bilby.io.db
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos
import com.blueskiron.bilby.io.db.ActiveSlickRepos.VisitorRepo
import com.blueskiron.bilby.io.db.ModelImplicits._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.FlatSpec
import io.strongtyped.active.slick.JdbcProfileProvider
import com.blueskiron.bilby.io.db.Tables.VisitorRow
import com.blueskiron.bilby.io.model.User
import java.util.UUID
import com.blueskiron.bilby.io.model.Countries
import com.blueskiron.bilby.io.db.Tables
import scala.concurrent.Future

class TestActiveSlickRepos extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  "This test" should " have access to test database" in {
    val session = fixtures.testDatabase.createSession()
    val conn = session.conn
    assert(!conn.isClosed())
    conn.close()
    session.close()
  }

  "Follower, Visitor, UserProfile and User repos" should "support all CRUD operations" in {
    import ActiveSlickRepos._
    import User.userWithProfileAndVisitor
    import jdbcProfile.api._
    val initialCount = query(UserRepo.count)
    for {
      packed <- (fixtures.users, fixtures.userProfiles, fixtures.visitors).zipped.toList

    } yield {
      val user = packed._1
      val userProfile = packed._2
      val visitor = packed._3
      val extendedUser = userWithProfileAndVisitor(user, Some(userProfile), Some(visitor))
      log.info("testign CRUD on composite entity: {}", extendedUser)

      //CREATE
      val savedEntities = (
        //#1 userProfile (if any)
        commit(UserprofileRepo.save(userProfile)),
        //#2 visitor
        commit(VisitorRepo.save(visitor)))
      //#3 user 
      val userRow = userRowFromUserAndForeignKeys(user, Some(savedEntities._1.id), Some(savedEntities._2.id))
      val savedUser = commit(UserRepo.save(userRow))
      //#4 follower
      //val savedFollower = commit(FollowerRepo.save(follower))

      //READ (confirm create)
      query {
        for {
          userCount <- UserRepo.count
          userProfileCount <- UserprofileRepo.count
          visitorCount <- VisitorRepo.count
          //followerCount <- FollowerRepo.count
        } yield {
          Math.abs(initialCount - userCount) shouldBe 1
          Math.abs(initialCount - userProfileCount) shouldBe 1
          Math.abs(initialCount - visitorCount) shouldBe 1
          //Math.abs(initialCount - followerCount) shouldBe 1
        }
      }

      //UPDATE

      //update user
      val _password = UUID.randomUUID().toString()
      val updatedUser = commit(UserRepo.save(savedUser.copy(password = _password)))
      //read (confirm update)
      updatedUser shouldBe query(UserRepo.findById(savedUser.id))
      updatedUser.password shouldBe _password

      //update visitor
      val _host = "123.25.0.2"
      val updatedVisitor = commit(VisitorRepo.save(savedEntities._2.copy(host = _host)))
      //read (confirm update)
      val queriedVisitor = query(VisitorRepo.findById(savedEntities._2.id))
      queriedVisitor shouldBe updatedVisitor
      queriedVisitor.host shouldBe _host
      
      //update userProfile 
      val _country = Some(Countries.list.reverse.head)
      val updatedUserProfile = commit(UserprofileRepo.save(savedEntities._1.copy(country = _country)))
      //read (confirm update)
      val queriedUp = query(UserprofileRepo.findById(savedEntities._1.id))
      queriedUp shouldBe updatedUserProfile
      queriedUp.country shouldBe _country

      //DELETE
      commit(UserRepo.deleteById(savedUser.id))
      commit(UserprofileRepo.deleteById(savedEntities._1.id))
      commit(VisitorRepo.deleteById(savedEntities._2.id))
      
      //read (confirm delete)
      query(UserRepo.findOptionById(savedUser.id)) shouldBe None
    }
  }

}