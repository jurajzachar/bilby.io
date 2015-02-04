package db

import models._
import scala.util.Random
import java.net.URL
import play.api.libs.json._

trait MockCase {
  import JsonConversions._
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
  //println(Json.toJson(piece))
}