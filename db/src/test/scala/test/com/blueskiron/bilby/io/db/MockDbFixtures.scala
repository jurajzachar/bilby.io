package test.com.blueskiron.bilby.io.db

import scala.util.Random
import scala.io.Source
import play.api.libs.json.Json
import com.blueskiron.bilby.io.api.model._
import com.blueskiron.bilby.io.api.model.JsonConversions._
import slick.jdbc.JdbcBackend.Database

/**
 * @author juri
 */
object MockBilbyFixtures {
  
  val testDatabase: Database = {
    val db = Database.forConfig("test_db")
    db.createSession().conn.setAutoCommit(true)
    db
  }
  
  val mockSize = 1000;
  val random = new Random(System.nanoTime())
  //read mock data from fs
  
  val accounts = Json.parse(Source.fromURL(getClass.getResource("/mock_accounts.json")).mkString).validate[Seq[Account]].get
  assert(accounts != null)
  println("mock accounts initialized: " + accounts.head)
  
  val users = for {
    (userName, account) <- (Json.parse(Source.fromURL(getClass.getResource("/mock_usernames.json")).mkString).validate[Seq[String]].get, accounts).zipped.toList
  } yield Json.obj(
      "userName" -> userName,
      "account" -> account).validate[User].get
  assert(users != null)
  println("mock users initialized: " + users.head)
  
  val userProfiles = Json.parse(Source.fromURL(getClass.getResource("/mock_userprofiles.json")).mkString).validate[Seq[UserProfile]].get
  assert(userProfiles != null)
  println("mock user profiles initialized: " + userProfiles.head)
  val visitors = Json.parse(Source.fromURL(getClass.getResource("/mock_visitors.json")).mkString).validate[Seq[Visitor]].get
  assert(visitors != null)
  println("mock visitors initialized: " + visitors.head)
  val followers = for (i <- (1 until users.size)) yield Follower(users.slice(i, random.nextInt(mockSize/4)).toSet, None)
  assert(followers != null)
  //warning: very hacky and ugly!
  val srcSeq = Source.fromURL(getClass.getResource("/sample_piece.md")).getLines.take(5).toIndexedSeq
  val pieceHeader = AssetHeader(
    srcSeq(0),
    Some(srcSeq(4)),
    Some(srcSeq(3)),
    Set("#test"),
    srcSeq.dropWhile(x => x.contains("---")).mkString)
  val piece = Asset(pieceHeader, None, 1, None)
  println("mock piece initialized: " + piece)
}