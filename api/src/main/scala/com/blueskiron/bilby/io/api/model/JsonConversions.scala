package com.blueskiron.bilby.io.api.model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import org.joda.time.LocalDateTime
import com.mohiva.play.silhouette.api.LoginInfo

trait JsonConversions {

  import HstoreUtils._

  implicit val loginInfoWrites: Writes[Seq[LoginInfo]] = new Writes[Seq[LoginInfo]] {
    def writes(o: Seq[LoginInfo]) = {
      Json.toJson(hstoreMapToString(o.map(x => x.providerID -> x.providerKey).toMap))
    }
  }
    
  implicit val roleWrites: Writes[Set[Role]] = new Writes[Set[Role]] {
    def writes(o: Set[Role]) = Json.toJson(o.seq.mkString(","))
  }

  implicit val localDateWrite: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(ld: LocalDateTime) = Json.toJson(ld.toDateTime().getMillis)
  }
  
  implicit val userReads: Reads[User] = (
    (JsPath \ "id").readNullable[Long] and
    (JsPath \ "username").read[String] and
    (JsPath \ "profiles").read[String].map(str => hstoreMapFromString(str).map(kv => LoginInfo(kv._1, kv._2)).toSeq) and
    (JsPath \ "roles").read[String].map(data => data.split(",").toList.map(Role(_)).toSet) and
    (JsPath \ "active").read[Boolean] and
    (JsPath \ "created").read[Long].map(new LocalDateTime(_)))(User.apply _)

  implicit val userProfileReads: Reads[UserProfile] = ((
    (JsPath \ "provider").read[String] and
    (JsPath \ "key").read[String])(LoginInfo.apply _) and
    (JsPath \ "email").readNullable[String] and
    (JsPath \ "firstname").readNullable[String] and
    (JsPath \ "lastname").readNullable[String] and
    (JsPath \ "fullname").readNullable[String] and
    (JsPath \ "avatarUrl").readNullable[String] and
    (JsPath \ "verified").read[Boolean] and
    (JsPath \ "created").read[Long].map(new LocalDateTime(_)))(UserProfile.apply _)

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").writeNullable[Long] and
    (JsPath \ "username").write[String] and
    (JsPath \ "profiles").write[Seq[LoginInfo]] and
    (JsPath \ "roles").write[Set[Role]] and
    (JsPath \ "active").write[Boolean] and
    (JsPath \ "created").write[LocalDateTime])(unlift(User.unapply))

}

object JsonConversions extends JsonConversions

