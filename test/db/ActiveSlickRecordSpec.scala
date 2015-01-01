package db

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfter
import play.api.libs.json._
import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import example.db.common.Util
import models._

class ActiveSlickRecordSpec extends FlatSpec with PostgresSpec with MustMatchers with BeforeAndAfter {
  
  import PostgresSpec.cake._
  
  before {
    //Util.loadDrivers
    database withSession {
      implicit session =>
      println("Before method creates schema")
      try{
        createSchema
      }
      catch {
        case t: Throwable => t.printStackTrace() // TODO: handle error
      }
    }
  }

  after {
    database withSession {
      implicit session =>
      println("After method drops schema.")
      dropSchema
    }
    //Util.unloadDrivers
  }

  trait Case {
    val MOCK_SIZE = 1000;
    val threePlayers = Player("Pelé") :: Player("Maradona") :: Player("Zico") :: Nil
    //read mock data from fs
    val users = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_USER.json").mkString).validate[Seq[User]].get
    val userProfiles = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_USERPROFILE.json").mkString).validate[Seq[UserProfile]].get
    val visitors = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_VISITOR.json").mkString).validate[Seq[Visitor]].get
  }

  "save" should " persist players in players table" in new Case {
    database withTransaction { implicit session =>
      threePlayers.foreach(_.save)
      val persistedPlayers = Players.fetchAll
      persistedPlayers.foreach(println(_))
      persistedPlayers.size must be (3)
    }
  }
  
  "save" should " persist visitors in visitors table" in new Case {
    database withTransaction { implicit session =>
      visitors.foreach(_.save)
      val persisted = Visitors.fetchAll
      persisted.foreach(println(_))
      persisted.size must be (MOCK_SIZE)
    }
  }
  
  "save" should " persist users in users table" in new Case {
    database withTransaction { implicit session =>
      users.foreach(_.save)
      val persisted = Visitors.fetchAll
      persisted.foreach(println(_))
      persisted.size must be (MOCK_SIZE)
    }
  }
  
  "save" should " persist user profiles in user profiles table" in new Case {
    database withTransaction { implicit session =>
      userProfiles.foreach(_.save)
      val persisted = Visitors.fetchAll
      persisted.foreach(println(_))
      persisted.size must be (MOCK_SIZE)
    }
  }
  
  //    "register" should "create a record in the user table" in new Case {
  //      dao.register(user)
  //      database withSession {
  //        Q.queryNA[Int]("select count(*) from public.user").first() must be(1)
  //      }
  //    }
  //  
  //    it should "allow multiple records" in new Case {
  //      dao.register(user)
  //      dao.register(user2) must be(true)
  //      database withSession {
  //        Q.queryNA[Int]("select count(*) from public.user").first() must be(2)
  //      }
  //    }

  // ... more tests ...
}