package com.blueskiron.bilby.io.mock

import scala.util.Random
import scala.io.Source
import play.api.libs.json.Json
import com.blueskiron.bilby.io.api.model._
import com.blueskiron.bilby.io.api.model.JsonConversions._
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable

/**
 * @author juri
 */
object MockBilbyFixtures {

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
  assert(!users.isEmpty)
  
  val userProfiles = Json.parse(Source.fromURL(getClass.getResource("/mock_userprofiles.json")).mkString).validate[Seq[UserProfile]].get
  assert(userProfiles != null)
  assert(!userProfiles.isEmpty)
  
  val visitors = Json.parse(Source.fromURL(getClass.getResource("/mock_visitors.json")).mkString).validate[Seq[Visitor]].get
  assert(visitors != null)
  assert(!visitors.isEmpty)
  
  val followers = for (i <- (1 until users.size)) yield Follower(users.slice(i, random.nextInt(mockSize / 4)).toSet, None)
  assert(followers != null)
  assert(!followers.isEmpty)
  
  //warning: very hacky and ugly!
  val srcSeq = Source.fromURL(getClass.getResource("/sample_asset.md")).getLines.take(5).toIndexedSeq
  val assetHeader = AssetHeader(
    srcSeq(0),
    Some(srcSeq(4)),
    Some(srcSeq(3)),
    Set("#test"),
    srcSeq.dropWhile(x => x.contains("---")).mkString)
  val asset = Asset(assetHeader, None, 1, None)
  println("DEBUG: mock piece initialized: " + asset)
  
  lazy val compiledUsers = {
     for {
      data <- ((users, accounts).zipped.toList, (userProfiles, visitors).zipped.toList).zipped.toList
    } yield (
      data._1._1, //user
      data._1._2, //account
      data._2._1, //userProfile
      data._2._2) //visitor
  }
}