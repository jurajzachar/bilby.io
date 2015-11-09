package com.blueskiron.bilby.io.api.model

import java.net.URL
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JsPath
import play.api.libs.json.JsString
import play.api.libs.json.Reads
import play.api.libs.json.Writes

object JsonConversions extends JsonReadWrite

trait JsonReadWrite {

  // to-do: get functional and fix me!   
  implicit val tagWrite: Writes[HashTag] = Writes {
    (tag: HashTag) => JsString(tag.xs)
  }
  implicit val urlWrite: Writes[URL] = Writes {
    (url: URL) => JsString(url.toString)
  }

  implicit val assetHeaderReads: Reads[AssetHeader] = (
    (JsPath \ "title").read[String] and
    (JsPath \ "short_summary").readNullable[String] and
    (JsPath \ "title_cover").readNullable[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "source").read[String])(AssetHeader.apply _)

  implicit val assetHeaderWrites: Writes[AssetHeader] = (
    (JsPath \ "title").write[String] and
    (JsPath \ "short_summary").writeNullable[String] and
    (JsPath \ "title_cover").writeNullable[String] and
    (JsPath \ "tags").write[Set[String]] and
    (JsPath \ "source").write[String])(unlift(AssetHeader.unapply))

  implicit val assetWrites: Writes[Asset] = (
    (JsPath \ "header").write[AssetHeader] and
    (JsPath \ "published").writeNullable[Long] and
    (JsPath \ "author_id").write[Long] and
    (JsPath \ "id").writeNullable[Long])(unlift(Asset.unapply))

  implicit val assetReads: Reads[Asset] = (
    (JsPath \ "header").read[AssetHeader] and
    (JsPath \ "published").readNullable[Long] and
    (JsPath \ "author_id").read[Long] and
    (JsPath \ "id").readNullable[Long])(Asset.apply _)

  implicit val accountReads: Reads[Account] = (
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "avatar_url").read[String] and
    (JsPath \ "auth_method").read[String] and
    (JsPath \ "oauth1").readNullable[String] and
    (JsPath \ "oauth2").readNullable[String] and
    (JsPath \ "password_info").readNullable[String] and
    (JsPath \ "verified").read[Boolean] and
    (JsPath \ "active").read[Boolean] and
    (JsPath \ "id").readNullable[Long])(Account.apply _)

  implicit val accountWrites: Writes[Account] = (
    (JsPath \ "email").write[String] and
    (JsPath \ "password").write[String] and
    (JsPath \ "avatar_url").write[String] and
    (JsPath \ "auth_method").write[String] and
    (JsPath \ "oauth1").writeNullable[String] and
    (JsPath \ "oauth2").writeNullable[String] and
    (JsPath \ "password_info").writeNullable[String] and
    (JsPath \ "verified").write[Boolean] and
    (JsPath \ "active").write[Boolean] and
    (JsPath \ "id").writeNullable[Long])(unlift(Account.unapply))

  implicit val userReads: Reads[User] = (
    (JsPath \ "userName").read[String] and
    (JsPath \ "account").read[Account] and
    (JsPath \ "user_profile").readNullable[UserProfile] and
    (JsPath \ "visitor").readNullable[Visitor] and
    (JsPath \ "id").readNullable[Long])(User.apply _)

  implicit val userWrites: Writes[User] = (
    (JsPath \ "user_name").write[String] and
    (JsPath \ "account").write[Account] and
    (JsPath \ "user_profile").writeNullable[UserProfile] and
    (JsPath \ "visitor").writeNullable[Visitor] and
    (JsPath \ "id").writeNullable[Long])(unlift(User.unapply))

  implicit val userProfileReads: Reads[UserProfile] = (
    (JsPath \ "first_name").readNullable[String] and
    (JsPath \ "last_name").readNullable[String] and
    (JsPath \ "country").readNullable[String] and
    (JsPath \ "place_of_res").readNullable[String] and
    (JsPath \ "age").readNullable[Short] and
    (JsPath \ "id").readNullable[Long])(UserProfile.apply _)

  implicit val userProfileWrites: Writes[UserProfile] = (
    (JsPath \ "first_name").writeNullable[String] and
    (JsPath \ "last_name").writeNullable[String] and
    (JsPath \ "country").writeNullable[String] and
    (JsPath \ "place_of_res").writeNullable[String] and
    (JsPath \ "age").writeNullable[Short] and
    (JsPath \ "id").writeNullable[Long])(unlift(UserProfile.unapply))

  implicit val visitorReads: Reads[Visitor] = (
    (JsPath \ "host").read[String] and
    (JsPath \ "timestamp").read[Long] and
    (JsPath \ "id").readNullable[Long])(Visitor.apply _)

  implicit val visitorWrites: Writes[Visitor] = (
    (JsPath \ "host").write[String] and
    (JsPath \ "timestamp").write[Long] and
    (JsPath \ "id").writeNullable[Long])(unlift(Visitor.unapply))
}