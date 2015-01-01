package components

import play.api.libs.json._
import play.api.libs.functional.syntax._
import io.strongtyped.active.slick.ActiveSlick
import scala.slick.jdbc.JdbcBackend
import scala.util.Try
import models._

trait SchemaExtensions {

  this: ActiveSlick with Schema =>

  import jdbcDriver.simple._

  val Players = EntityTableQuery[Player, PlayersTable](tag => new PlayersTable(tag))
  val Visitors = EntityTableQuery[Visitor, VisitorsTable](tag => new VisitorsTable(tag))
  val Users = EntityTableQuery[User, UsersTable](tag => new UsersTable(tag))
  val UserProfiles = EntityTableQuery[UserProfile, UserProfilesTable](tag => new UserProfilesTable(tag))

  val ddl = Players.ddl ++ Visitors.ddl ++ Users.ddl ++ UserProfiles.ddl

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

  //Json stuff
  implicit val userReads: Reads[User] = (
    (JsPath \ "firstName").readNullable[String] and
    (JsPath \ "lastName").readNullable[String] and
    (JsPath \ "username").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "email").read[String] and
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
