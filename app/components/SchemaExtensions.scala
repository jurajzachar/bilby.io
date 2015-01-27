package components

import play.api.libs.json._
import play.api.libs.functional.syntax._
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.jdbc.JdbcBackend
import scala.util.Try
import models._
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob
import java.net.URL

trait SchemaExtensions {

  this: ActiveSlick with Schema =>

  import jdbcDriver.simple._

  lazy val Visitors = EntityTableQuery[Visitor, VisitorsTable](tag => new VisitorsTable(tag))
  lazy val Followers = FollowersTable
  lazy val Users = EntityTableQuery[User, UsersTable](tag => new UsersTable(tag))
  lazy val UserProfiles = EntityTableQuery[UserProfile, UserProfilesTable](tag => new UserProfilesTable(tag))
  lazy val Pieces = EntityTableQuery[Piece, PiecesTable](tag => new PiecesTable(tag))

  val ddl = Visitors.ddl ++ UserProfiles.ddl ++ Users.ddl ++ Followers.ddl ++ Pieces.ddl

  implicit class VisitorExtenstions(val model: Visitor) extends ActiveRecord[Visitor] {
    override def table = Visitors
  }

  implicit class UsersExtensions(val model: User) extends ActiveRecord[User] {
    override def table = Users
  }

  implicit class UserProfilesExtensions(val model: UserProfile) extends ActiveRecord[UserProfile] {
    override def table = UserProfiles
  }

  implicit class PiecesExtensions(val model: Piece) extends ActiveRecord[Piece] {
    override def table = Pieces
  }

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
    (JsPath \ "email").readNullable[String] and
    (JsPath \ "password").readNullable[String] and
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
