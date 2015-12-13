package com.blueskiron.bilby.io.api.model

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JsPath
import play.api.libs.json.JsString
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.JsBoolean
import org.joda.time.LocalDateTime
import com.mohiva.play.silhouette.api.LoginInfo

trait JsonConversions {

  import HstoreUtils._

  implicit val userReads: Reads[User] = (
    (JsPath \ "id").readNullable[Long] and
    (JsPath \ "username").readNullable[String] and
    (JsPath \ "profiles").read[String].map(str => hstoreMapFromString(str).map(kv => LoginInfo(kv._1, kv._2)).toSeq) and
    (JsPath \ "roles").read[String].map(data => data.split(",").toList.map(Role(_)).toSet) and
    (JsPath \ "active").read[Boolean] and
    (JsPath \ "created").read[Long].map(new LocalDateTime(_)))(User.apply _)

  //to-do writes...
}

object JsonConversions extends JsonConversions

