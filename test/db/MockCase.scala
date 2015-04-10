package db

import scala.util.Random
import java.net.URL
import play.api.libs.json._
import components.JsonConversions._
import models.Follower
import models.UserProfile
import models.PieceFormInfo
import models.HashTag
import models.Piece
import models.Visitor
import models.User
import models.Follower

trait MockCase {
  val MOCK_SIZE = 1000;
  val random = new Random(System.nanoTime())
  //read mock data from fs
  val users = Json.parse(scala.io.Source.fromFile("resources/MOCK_DATA_USER.json").mkString).validate[Seq[User]].get
  val connectedUsers = for (u <- users) yield new User(
    u.firstName,
    u.lastName,
    u.username,
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
  val srcSeq = scala.io.Source.fromFile("resources/sample_piece.md").getLines.take(5).toIndexedSeq
  val pieceHeader = PieceFormInfo(
    srcSeq(0),
    srcSeq(4),
    new URL(srcSeq(3)),
    Set("#test"),
    srcSeq.dropWhile(x => x.contains("---")).mkString  
  )

  val piece = Piece(pieceHeader, None, 1, None)
  //println(Json.toJson(piece))
}