package components

import java.net.URL
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JsPath
import play.api.libs.json.JsString
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import models.HashTag
import models.Piece
import models.User
import models.UserProfile
import models.Visitor
import models.PieceFormInfo

object JsonConversions extends JsonReadWrite

trait JsonReadWrite {

  // to-do: get functional and fix me!   
  implicit val tagWrite: Writes[HashTag] = Writes {
    (tag: HashTag) => JsString(tag.xs)
  }
  implicit val urlWrite: Writes[URL] = Writes {
    (url: URL) => JsString(url.toString)
  }

  implicit val pieceHeaderReads: Reads[PieceFormInfo] = (
    (JsPath \ "title").read[String] and
    (JsPath \ "shortSummary").read[String] and
    (JsPath \ "titleCover").read[String].map(new URL(_)) and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "source").read[String])(PieceFormInfo.apply _)

  implicit val pieceHeaderWrites: Writes[PieceFormInfo] = (
    (JsPath \ "title").write[String] and
    (JsPath \ "shortSummary").write[String] and
    (JsPath \ "titleCover").write[URL] and
    (JsPath \ "tags").write[Set[String]] and
    (JsPath \ "source").write[String])(unlift(PieceFormInfo.unapply))

  implicit val pieceWrites: Writes[Piece] = (
    (JsPath \ "header").write[PieceFormInfo] and
    (JsPath \ "published").writeNullable[Long] and
    (JsPath \ "authorId").write[Long] and
    (JsPath \ "rating").write[Double] and
    (JsPath \ "id").writeNullable[Long])(unlift(Piece.unapply))
    
  implicit val pieceReads: Reads[Piece] = (
    (JsPath \ "header").read[PieceFormInfo] and
    (JsPath \ "published").readNullable[Long] and
    (JsPath \ "authorId").read[Long] and
    (JsPath \ "rating").read[Double] and
    (JsPath \ "id").readNullable[Long])(Piece.apply _)

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