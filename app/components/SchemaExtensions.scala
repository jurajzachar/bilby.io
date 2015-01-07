package components

import play.api.libs.json._
import play.api.libs.functional.syntax._
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.jdbc.JdbcBackend
import scala.util.Try
import models._
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

trait SchemaExtensions {

  this: ActiveSlick with Schema =>

  import jdbcDriver.simple._

  lazy val Players = EntityTableQuery[Player, PlayersTable](tag => new PlayersTable(tag))
  lazy val Visitors = EntityTableQuery[Visitor, VisitorsTable](tag => new VisitorsTable(tag))
  lazy val Followers = EntityTableQuery[Follower, FollowersTable](tag => new FollowersTable(tag))
  lazy val Users = EntityTableQuery[User, UsersTable](tag => new UsersTable(tag))
  lazy val UserProfiles = EntityTableQuery[UserProfile, UserProfilesTable](tag => new UserProfilesTable(tag))

  val ddl = Players.ddl ++ Visitors.ddl ++ UserProfiles.ddl ++ Users.ddl ++ Followers.ddl

  implicit class PlayersExtensions(val model: Player) extends ActiveRecord[Player] {
    override def table = Players
  }

  implicit class VisitorExtenstions(val model: Visitor) extends ActiveRecord[Visitor] {
    override def table = Visitors
  }

  implicit class UsersExtensions(val model: User) extends ActiveRecord[User] {
    override def table = Users
  }

  implicit class UserProfilesExtensions(val model: UserProfile) extends ActiveRecord[UserProfile] {
    override def table = UserProfiles
  }

  implicit class FollowersExtensions(val model: Follower) extends ActiveRecord[Follower] {
    override def table = Followers
  }

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
