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
import com.blueskiron.bilby.io.db.dao.UserProfilesDao
import com.blueskiron.bilby.io.db.dao.UserProfilesDao
import com.blueskiron.bilby.io.db.codegen.Tables.UserProfilesRow
import scala.concurrent.Promise

/**
 * @author juri
 */
class UserProfilesDaoSpec extends FlatSpec with PostgresSuite with UserProfilesDao {

  import Driver.api._

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  lazy val sampleDataPromise: Promise[Map[User, Seq[UserProfilesRow]]] = Promise()
  import fixtures._

  override def beforeAll = {

    //start with a blank slate
    log.info("cleaning up database...")
    cleanUp()

    for (mockUser <- users) {
      val saved = awaitResult(userProfilesDao.cake.commit(UsersRepo.save(ModelImplicits.ToDataRow.rowFromUser(mockUser))))
    }

    //create map of User -> UserProfiles
    val usersWithProfiles = {
      for {
        user <- users
      } yield user -> user.profiles.map { linfo => 
        UserProfilesRow(
          linfo.providerID,
          linfo.providerKey, None, None, None, None, None,
          true, new java.sql.Timestamp(System.currentTimeMillis()))
      }
    }.toMap

    //insert userProfiles
    val res = usersWithProfiles.values.foldLeft(0) {
      (agg, rows) =>
        (awaitResult(userProfilesDao.cake.runAction(Tables.UserProfiles ++= rows)).getOrElse(0) + agg)
    }
    log.debug("Inserted {} user profiles", res)

    sampleDataPromise.success(usersWithProfiles)

  }

  "UserProfilesDao" should "have sample data available" in {
    sampleDataPromise.isCompleted shouldBe true
  }

  "UserProfilesDao" should "be able to find user profile" in {
    import ModelImplicits.ToModel
    val sampleData = awaitResult(sampleDataPromise.future)
    for {
      expectedUser <- sampleData.keys
      profiles = sampleData(expectedUser)
      p <- profiles
    } awaitResult(userProfilesDao.findUserProfile(p.provider, p.key)) map {
      up => up shouldBe ToModel.userProfileFromRow(p)
    }
  }

  "UserProfilesDao" should "be bale to find user profiles by username" in {
    import ModelImplicits.ToModel
    val sampleData = awaitResult(sampleDataPromise.future)
    for (expectedUser <- sampleData.keys) {
      val expectedUsername = expectedUser.username.getOrElse("error")
      //log.debug("expected user: {}", expectedUsername)
      val profiles = sampleData(expectedUser).toSet map ToModel.userProfileFromRow _
      //log.debug("expected profiles: {}", profiles)
      awaitResult(userProfilesDao.findUserProfiles(expectedUsername)) shouldBe profiles
    }
  }
  override lazy val userProfilesDao = initWithApplicationDatabase(this)

} 