package db

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfter
import play.api.libs.json._
import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import models._
import scala.util.Random
import org.slf4j.LoggerFactory
import java.net.URL

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
    val headerSeq = scala.io.Source.fromFile("resources/sample_piece.md").getLines.take(5).toIndexedSeq
    println(headerSeq)
    val pieceHeader: PieceHeader = PieceHeader(
        headerSeq(0), 
        headerSeq(4), 
        new URL(headerSeq(3)), 
        java.lang.Long.parseLong(headerSeq(1)), 
        1, 
        Set(HashTag("#test")), 
        java.lang.Double.parseDouble(headerSeq(2)))
  
    val pieceSeq = scala.io.Source.fromFile("resources/sample_piece.md").getLines.dropWhile(x => x.contains("---"))
    val piece = Piece(None, pieceHeader, pieceSeq.mkString)
    println(Json.toJson(piece))
  }
  
  "save" should " persist records in their respective tables" in new Case {
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
          println(x.toString)
      }
    }
    //Followers (non-active record)
    database withTransaction { implicit session =>
      followers.foreach(Followers += _)
    }
    database withTransaction { implicit session =>
      Followers.length.run must be(1000)
      Followers.list.foreach {
        x =>
          {
            val user = Users.findById(x.id)
            val leads: Set[User] = for (id <- x.fids) yield Users.findById(id)
            //println(s"user ${user.userName} follows:[${leads.map(x => x.userName).mkString(",")}]")
            println(s"user ${user.userName} follows: ${leads.size} other users.")
          }
      }
    }
  }

}