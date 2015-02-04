package models

import java.net.URL

import org.mindrot.jbcrypt.BCrypt

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

  implicit val pieceHeaderReads: Reads[PieceHeader] = (
    (JsPath \ "title").read[String] and
    (JsPath \ "shortSummary").read[String] and
    (JsPath \ "titleCover").read[String].map(new URL(_)) and
    (JsPath \ "published").read[Long] and
    (JsPath \ "authorId").read[Long] and
    (JsPath \ "tags").read[Set[String]].map(x => x.map(HashTag(_))) and
    (JsPath \ "rating").read[Double])(PieceHeader.apply _)

  implicit val pieceHeaderWrites: Writes[PieceHeader] = (
    (JsPath \ "title").write[String] and
    (JsPath \ "shortSummary").write[String] and
    (JsPath \ "titleCover").write[URL] and
    (JsPath \ "published").write[Long] and
    (JsPath \ "authorId").write[Long] and
    (JsPath \ "tags").write[Set[HashTag]] and
    (JsPath \ "rating").write[Double])(unlift(PieceHeader.unapply))

  implicit val pieceWrites: Writes[Piece] = (
    (JsPath \ "id").writeNullable[Long] and
    (JsPath \ "header").write[PieceHeader] and
    (JsPath \ "source").write[String])(unlift(Piece.unapply))

  implicit val userReads: Reads[User] = (
    (JsPath \ "firstName").readNullable[String] and
    (JsPath \ "lastName").readNullable[String] and
    (JsPath \ "username").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String].map(BCrypt.hashpw(_, BCrypt.gensalt())) and
    (JsPath \ "avatarUrl").read[String] and
    (JsPath \ "authMethod").read[String] and
    (JsPath \ "oauth1").readNullable[String] and
    (JsPath \ "oauth2").readNullable[String] and
    (JsPath \ "passwordInfo").readNullable[String] and
    (JsPath \ "userprofileId").readNullable[Long] and
    (JsPath \ "visitorId").readNullable[Long] and
    (JsPath \ "id").readNullable[Long])(User.apply _)

  implicit val userProfileReads: Reads[UserProfile] = (
    (JsPath \ "country").readNullable[String] and
    (JsPath \ "placeOfResidence").readNullable[String] and
    (JsPath \ "age").readNullable[Short] and
    (JsPath \ "id").readNullable[Long])(UserProfile.apply _)

  implicit val visitorReads: Reads[Visitor] = (
    (JsPath \ "host").readNullable[String] and
    (JsPath \ "timestamp").read[Long] and
    (JsPath \ "id").readNullable[Long])(Visitor.apply _)
}