package test.com.blueskiron.bilby.io.core

import scala.util.Random
import scala.io.Source
import play.api.libs.json.Json
import com.blueskiron.bilby.io.api.model._
import com.blueskiron.bilby.io.api.model.JsonConversions._

/**
 * @author juri
 */
object MockCoreFixtures {
  
  val mockSize = 1000;
  val random = new Random(System.nanoTime())
  //read mock data from fs
  val users = Json.parse(Source.fromURL(getClass.getResource("/mock_users.json")).mkString).validate[Seq[User]].get
  assert(users != null)
  val userProfiles = Json.parse(Source.fromURL(getClass.getResource("/mock_userprofiles.json")).mkString).validate[Seq[UserProfile]].get
  assert(userProfiles != null)
  val visitors = Json.parse(Source.fromURL(getClass.getResource("/mock_visitors.json")).mkString).validate[Seq[Visitor]].get
  assert(visitors != null)
  val followers = for (i <- (1 until 1001)) yield Follower(None, Set(i.toLong to random.nextInt(mockSize/4).toLong: _*))
  assert(followers != null)
  //warning: very hacky and ugly!
  val srcSeq = Source.fromURL(getClass.getResource("/sample_piece.md")).getLines.take(5).toIndexedSeq
  val pieceHeader = PieceHeader(
    srcSeq(0),
    Some(srcSeq(4)),
    Some(srcSeq(3)),
    Set("#test"),
    srcSeq.dropWhile(x => x.contains("---")).mkString)
  val piece = Piece(pieceHeader, None, 1, None)
}