package db

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfter
import play.api.libs.json._
import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import models._
import scala.util.Random

class ActiveSlickRecordSpec extends FlatSpec with PostgresSpec with MustMatchers with BeforeAndAfter {

  import PostgresSpec.cake._

  before {
    database withSession {
      implicit session =>
        println("Before method creates schema")
        try {
          createSchema
        } catch {
          case t: Throwable => println(t.getMessage) // TODO: handle error
        }
    }

  }

  after {
    database withSession {
      implicit session =>
        try {
          dropSchema
        } catch {
          case t: Throwable => t.printStackTrace() // TODO: handle error
        }
    }
  }

  trait Case {
    val MOCK_SIZE = 1000;
    val random = new Random(System.nanoTime())
    val threePlayers = Player("Pelé") :: Player("Maradona") :: Player("Zico") :: Nil
    //read mock data from fs
    val users = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_USER.json").mkString).validate[Seq[User]].get
    val connectedUsers = for (u <- users) yield new User(
      u.firstName,
      u.lastName,
      u.userName,
      u.email,
      u.password,
      u.avatarUrl,
      u.authMethod,
      u.oAuth1Info,
      u.oAuth2Info,
      u.passwordInfo,
      Some(random.nextInt(999) + 1), //userprofile_id 
      Some(random.nextInt(999) + 1)) //visitor_id
    val userProfiles = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_USERPROFILE.json").mkString).validate[Seq[UserProfile]].get
    val visitors = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_VISITOR.json").mkString).validate[Seq[Visitor]].get
    val followers = for (i <- (1 until 1001)) yield Follower(i, Set(1L to random.nextInt(MOCK_SIZE).toLong: _*))
  }

  "save" should " persist players in players table" in new Case {
    database withTransaction { implicit session =>
      threePlayers.foreach(_.save)
      val persistedPlayers = Players.fetchAll
      persistedPlayers.size must be(3)
    }
  }

  "save" should " persist visitor in visitors table" in new Case {
    //1st round --> visitor
    database withTransaction { implicit session =>
      visitors.foreach(_.save)
      Visitors.count must be(1000)
    }
  }

  "save" should " persist user profile in user profiles table" in new Case {
    database withTransaction { implicit session =>
      userProfiles.size must be(MOCK_SIZE)
      userProfiles.foreach(_.save)
      UserProfiles.count must be(1000)
    }
  }

  "save" should " persist user and follower in users and followers tables" in new Case {
    import jdbcDriver.simple._
    //Vistors
    database withTransaction { implicit session =>
      visitors.foreach(_.save)
    }
    //UserProfiles
    database withTransaction { implicit session =>
      userProfiles.foreach(_.save)
    }
    //Users
    database withTransaction { implicit session =>
      //randomly assign user profiles to users.
      connectedUsers.foreach(_.save) //persist users
      Users.count must be(1000)
      Users.fetchAll.foreach {
        x =>
          x.userProfile = UserProfiles.findById(x.userprofile_id.getOrElse(-1))
          x.visitor = Visitors.findById(x.visitor_id.getOrElse(-1))
          println(x)
      }
    }
    //Followers
    database withTransaction { implicit session =>
      followers.foreach(_.save)
      Followers.count must be(1000)
      Followers.fetchAll.foreach { 
        x => {
          val user = Users.findById(x.userId)
          val leads:Set[User] = for (id <- x.fids) yield Users.findById(id)
          println(s"user ${user.userName} follows:[${leads.map(x => x.userName).mkString(",")}]")
        }
      }
    }
  }

}