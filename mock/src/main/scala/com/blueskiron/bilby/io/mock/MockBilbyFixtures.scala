package com.blueskiron.bilby.io.mock

import scala.util.Random
import scala.io.Source
import play.api.libs.json.Json
import com.blueskiron.bilby.io.api.model.User
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable
import com.blueskiron.bilby.io.api.model.AssetHeader
import com.blueskiron.bilby.io.api.model.Asset
import com.blueskiron.bilby.io.api.model.JsonConversions
import com.blueskiron.bilby.io.api.model.UserProfile
import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.LocalDateTime

/**
 * @author juri
 */
object MockBilbyFixtures extends JsonConversions {
  
  val dbConfigPath = "bilby.io.test-db"
    
  //read mock data from the fs
  val userProfiles = Json.parse(Source.fromURL(getClass.getResource("/mock_data.json")).mkString).validate[Seq[UserProfile]].get
  assert(userProfiles != null)
  
  private val users = Json.parse(Source.fromURL(getClass.getResource("/mock_data.json")).mkString).validate[Seq[User]].get
  assert(users != null)

  //warning: very hacky and ugly!
  val srcSeq = Source.fromURL(getClass.getResource("/sample_asset.md")).getLines.take(5).toIndexedSeq
  val assetHeader = AssetHeader(
    srcSeq(0),
    srcSeq(4),
    Some(srcSeq(3)),
    Set("#test"),
    srcSeq.dropWhile(x => x.contains("---")).mkString)
  val asset = Asset(assetHeader, None, 1, None)
  println("DEBUG: mock piece initialized: " + asset)

  def mockSize = users.size

  //create map of User -> UserProfiles
  def usersWithProfiles(profiles: Seq[UserProfile] = userProfiles): Map[User, Seq[UserProfile]] = {
    for {
      (user, userProfile) <- users.zip(profiles)
    } yield user.copy(profiles = Seq(userProfile.loginInfo)) -> Seq(userProfile) }.toMap
}
