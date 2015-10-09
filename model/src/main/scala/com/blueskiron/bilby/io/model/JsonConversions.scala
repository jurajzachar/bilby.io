package com.blueskiron.bilby.io.model

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
    (JsPath \ "shortSummary").readNullable[String] and
    (JsPath \ "titleCover").readNullable[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "source").read[String])(PieceHeader.apply _)

  implicit val pieceHeaderWrites: Writes[PieceHeader] = (
    (JsPath \ "title").write[String] and
    (JsPath \ "shortSummary").writeNullable[String] and
    (JsPath \ "titleCover").writeNullable[String] and
    (JsPath \ "tags").write[Set[String]] and
    (JsPath \ "source").write[String])(unlift(PieceHeader.unapply))

  implicit val pieceWrites: Writes[Piece] = (
    (JsPath \ "header").write[PieceHeader] and
    (JsPath \ "published").writeNullable[Long] and
    (JsPath \ "authorId").write[Long] and
    (JsPath \ "id").writeNullable[Long])(unlift(Piece.unapply))
    
  implicit val pieceReads: Reads[Piece] = (
    (JsPath \ "header").read[PieceHeader] and
    (JsPath \ "published").readNullable[Long] and
    (JsPath \ "authorId").read[Long] and
    (JsPath \ "id").readNullable[Long])(Piece.apply _)

  implicit val userReads: Reads[User] = (
    (JsPath \ "firstName").readNullable[String] and
    (JsPath \ "lastName").readNullable[String] and
    (JsPath \ "userName").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "avatarUrl").read[String] and
    (JsPath \ "authMethod").read[String] and
    (JsPath \ "oauth1").readNullable[String] and
    (JsPath \ "oauth2").readNullable[String] and
    (JsPath \ "passwordInfo").readNullable[String] and
    (JsPath \ "userprofile").readNullable[UserProfile] and
    (JsPath \ "visitor").readNullable[Visitor] and
    (JsPath \ "id").readNullable[Long])(User.apply _)
  
  implicit val userWrites: Writes[User] = (
    (JsPath \ "firstName").writeNullable[String] and
    (JsPath \ "lastName").writeNullable[String] and
    (JsPath \ "userName").write[String] and
    (JsPath \ "email").write[String] and
    (JsPath \ "password").write[String] and
    (JsPath \ "avatarUrl").write[String] and
    (JsPath \ "authMethod").write[String] and
    (JsPath \ "oauth1").writeNullable[String] and
    (JsPath \ "oauth2").writeNullable[String] and
    (JsPath \ "passwordInfo").writeNullable[String] and
    (JsPath \ "userprofile").writeNullable[UserProfile] and
    (JsPath \ "visitor").writeNullable[Visitor] and
    (JsPath \ "id").writeNullable[Long])(unlift(User.unapply))
    
  implicit val userProfileReads: Reads[UserProfile] = (
    (JsPath \ "country").readNullable[String] and
    (JsPath \ "placeOfResidence").readNullable[String] and
    (JsPath \ "age").readNullable[Short] and
    (JsPath \ "id").readNullable[Long])(UserProfile.apply _)
    
  implicit val userProfileWrites: Writes[UserProfile] = (
    (JsPath \ "country").writeNullable[String] and
    (JsPath \ "placeOfResidence").writeNullable[String] and
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