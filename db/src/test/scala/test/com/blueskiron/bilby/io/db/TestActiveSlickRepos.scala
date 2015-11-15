package test.com.blueskiron.bilby.io.db
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.FlatSpec
import io.strongtyped.active.slick.JdbcProfileProvider
import com.blueskiron.bilby.io.db.Tables.VisitorRow
import com.blueskiron.bilby.io.db.Tables.UserRow
import com.blueskiron.bilby.io.db.Tables.AccountRow
import com.blueskiron.bilby.io.db.Tables.AssetRow
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.Countries
import java.util.UUID
import com.blueskiron.bilby.io.db.Tables
import scala.concurrent.Future


class TestActiveSlickRepos extends FlatSpec with PostgresSuite {
  
  import com.blueskiron.bilby.io.db.ar.ModelImplicits._
  
  val log = LoggerFactory.getLogger(getClass)
  
  val fixtures = MockBilbyFixtures
  "This test" should " have access to test database" in {
    val session = fixtures.testDatabase.createSession()
    val conn = session.conn
    assert(!conn.isClosed())
    conn.close()
    session.close()
  }
  
  behavior of "ActiveSlick Entities"
  
  "Visitor, UserProfile, Account and User repos" should "support all CRUD operations" in {
    import jdbcProfile.api._
    val initialCount = query(UserRepo.count)
    for {
      userAccount <- (fixtures.users, fixtures.accounts).zipped.toList
      userExtras <- (fixtures.userProfiles, fixtures.visitors).zipped.toList
    } {
      val user = userAccount._1
      val account = userAccount._2
      val userProfile = userExtras._1
      val visitor = userExtras._2
      val extendedUser = User.create(None, user.userName, account, Some(userProfile), Some(visitor))
      log.info("testing CRUD operations on composite entity: {}", extendedUser)

      //CREATE
      val savedEntities = (
        //#1 account (mandatory)
        commit(AccountRepo.save(account)),
        //#2 userProfile (if any)
        commit(UserprofileRepo.save(userProfile)),
        //#2 visitor (if any)
        commit(VisitorRepo.save(visitor)))
      //#3 user 
      val userRow = rowFromUser(extendedUser)
      val savedUser = commit(UserRepo.save(userRow))

      //READ (confirm create)
      query {
        for {
          userCount <- UserRepo.count
          accountCount <- AccountRepo.count
          userProfileCount <- UserprofileRepo.count
          visitorCount <- VisitorRepo.count
          //followerCount <- FollowerRepo.count
        } yield {
          Math.abs(initialCount - userCount) shouldBe 1
          Math.abs(initialCount - accountCount) shouldBe 1
          Math.abs(initialCount - userProfileCount) shouldBe 1
          Math.abs(initialCount - visitorCount) shouldBe 1
          //Math.abs(initialCount - followerCount) shouldBe 1
        }
      }

      //UPDATE
     //update user
      val _userName = UUID.randomUUID().toString()
      val updatedUser = commit(UserRepo.save(savedUser.copy(userName = _userName)))
      //read (confirm update)
      updatedUser shouldBe query(UserRepo.findById(savedEntities._1.id))
      updatedUser.userName shouldBe _userName
      
      //update account
      val _password = UUID.randomUUID().toString()
      val updatedAccount = commit(AccountRepo.save(savedEntities._1.copy(password = _password)))
      //read (confirm update)
      updatedAccount shouldBe query(AccountRepo.findById(savedEntities._1.id))
      updatedAccount.password shouldBe _password

      //update visitor
      val _host = "123.25.0.2"
      val updatedVisitor = commit(VisitorRepo.save(savedEntities._3.copy(host = _host)))
      //read (confirm update)
      val queriedVisitor = query(VisitorRepo.findById(savedEntities._3.id))
      queriedVisitor shouldBe updatedVisitor
      queriedVisitor.host shouldBe _host
      
      //update userProfile 
      val _country = Some(Countries.list.reverse.head)
      val updatedUserProfile = commit(UserprofileRepo.save(savedEntities._2.copy(country = _country)))
      //read (confirm update)
      val queriedUp = query(UserprofileRepo.findById(savedEntities._2.id))
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
  
  "Asset" should "support all CRUD operations" in {
    import jdbcProfile.api._
    val initialCount = query(AssetRepo.count)
    //CREATE
    val savedUser = commit(UserRepo.save(rowFromUser(fixtures.users.head)))
    val piece = fixtures.piece.copy(authorId = savedUser.id)
    val savedPiece = commit(AssetRepo.save(piece))
    //READ
    query(AssetRepo.findById(savedPiece.id)) shouldBe savedPiece
    //UPDATE
    val _tags = Some(Set("foo", "bar").mkString(","))
    commit(AssetRepo.save(savedPiece.copy(tags = _tags)))
    //read (confirm update)
    val queriedPiece = query(AssetRepo.findById(savedPiece.id))
    queriedPiece.tags shouldBe _tags
    //DELETE
    commit(AssetRepo.deleteById(savedPiece.id))
    query(AssetRepo.findOptionById(savedPiece.id)) shouldBe None
    commit(UserRepo.deleteById(savedUser.id))
    query(UserRepo.findOptionById(savedUser.id)) shouldBe None
    
  }
  
  private def cleanUp() = {
    import jdbcProfile.api._
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
}