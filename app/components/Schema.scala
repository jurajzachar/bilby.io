package components

import io.strongtyped.active.slick.{ TableQueries, Tables, Profile }
import scala.slick.lifted.ProvenShape
import play.api.libs.json._
import models._

trait Schema { this: Tables with TableQueries with Profile =>

  import jdbcDriver.simple._

  class PlayersTable(tag: Tag) extends EntityTable[Player](tag, "player") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("player_name")

    def * = (name, id.?) <> (Player.tupled, Player.unapply)

  }

  /** Table description of Visitor. */
  class VisitorsTable(tag: Tag) extends EntityTable[Visitor](tag, "visitor") {

    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column host */
    val host: Column[Option[String]] = column[Option[String]]("host")
    /** Database column timestamp */
    val timestamp: Column[Long] = column[Long]("timestamp")
    /** Database column id AutoInc, PrimaryKey */

    def * = (host, timestamp, id.?) <> (Visitor.tupled, Visitor.unapply)

  }
  /** Collection-like TableQuery object for table User */
  lazy val VisitorsTable = new TableQuery(tag => new VisitorsTable(tag))

  /** Table description of USerProfile. */
  class UserProfilesTable(tag: Tag) extends EntityTable[UserProfile](tag, "userprofile") {

    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column country  */
    val country: Column[Option[String]] = column[Option[String]]("country")
    /** Database column place of residence  */
    val placeOfResidence: Column[Option[String]] = column[Option[String]]("place_of_res")
    /** Database column age  */
    val age: Column[Option[Short]] = column[Option[Short]]("age")

    def * = (country, placeOfResidence, age, id.?) <> (UserProfile.tupled, UserProfile.unapply)

  }
  /** Collection-like TableQuery object for table UsersProfile */
  lazy val UserProfilesTable = new TableQuery(tag => new UserProfilesTable(tag))

  /** Table description of table user. */
  class UsersTable(tag: Tag) extends EntityTable[User](tag, "user") {

    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first name optional **/
    val firstName: Column[Option[String]] = column[Option[String]]("first_name")
    /** Database column last name optional **/
    val lastName: Column[Option[String]] = column[Option[String]]("last_name")
    /** Database column username  */
    val userName: Column[String] = column[String]("user_name")
    /** Database column email  */
    val email: Column[Option[String]] = column[Option[String]]("email")
    /** Database column password (encrypted) */
    val password: Column[Option[String]] = column[Option[String]]("password")
    /** Database column avatar url **/
    val avatarUrl: Column[Array[Byte]] = column[Array[Byte]]("avatar_url")
    /** Database column authentication method. **/
    val authMethod: Column[String] = column[String]("auth_method")
    /** Database column oAuth1Info */
    val oauth1: Column[Option[String]] = column[Option[String]]("oauth1")
    /** Database column oAuth2Info */
    val oauth2: Column[Option[String]] = column[Option[String]]("oauth2")
    /** Database column PasswordInfo */
    val passwordInfo: Column[Option[String]] = column[Option[String]]("passwordInfo")

    /** Database column userprofile_id  */
    val userprofileId: Column[Long] = column[Long]("userprofile_id")
    /** Database column visitor_id **/
    val visitorId: Column[Long] = column[Long]("visitor_id")

    /** Uniqueness Index over (username) (database name unique_username) */
    val index1 = index("unique_username", userName, unique = true)

    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val userprofileFk = foreignKey("userprofile_id", userprofileId, UserProfilesTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val visitorFk = foreignKey("visitor_id", userprofileId, VisitorsTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    def * : ProvenShape[User] = {
      val shapedValue = (
        firstName,
        lastName,
        userName,
        email,
        password,
        avatarUrl,
        authMethod,
        oauth1,
        oauth2,
        passwordInfo,
        userprofileId.?,
        visitorId.?,
        id.?).shaped
      shapedValue.<>({
        tuple =>
          User.apply(
            tuple._1,
            tuple._2,
            tuple._3,
            tuple._4,
            tuple._5,
            new String(tuple._6.map(_.toChar)),
            tuple._7,
            tuple._8,
            tuple._9,
            tuple._10,
            tuple._11,
            tuple._12,
            tuple._13)
      }, {
        (u: User) =>
          Some {
            (u.firstName,
              u.lastName,
              u.userName,
              u.email,
              u.password,
              u.avatarUrl.getBytes,
              u.authMethod,
              u.oAuth1Info,
              u.oAuth2Info,
              u.passwordInfo,
              u.userprofile_id,
              u.visitor_id,
              u.id)
          }
      })
    }
  }

  /** Collection-like TableQuery object for table User */
  lazy val UsersTable = new TableQuery(tag => new UsersTable(tag))

  /** Table description of table user. */
  class FollowersTable(tag: Tag) extends EntityTable[Follower](tag, "follower") {
    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.PrimaryKey)
    val fids: Column[Array[Byte]] = column[Array[Byte]]("fids", O.NotNull)

    /** Foreign key referencing User (database name userprofile_id) */
    lazy val userFk = foreignKey("id", id, UsersTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    def * : ProvenShape[Follower] = {
      val shapedValue = (id, fids).shaped
      shapedValue.<>({
        tuple =>
          Follower.apply(tuple._1, Json.parse(new String(tuple._2).map(_.toChar)).as[Set[Long]])
      }, {
        (f: Follower) =>
          Some {
            (f.userId, Json.stringify(Json.toJson(f.fids)).getBytes)
          }
      })
    }
  }
    /** Collection-like TableQuery object for table User */
  lazy val FollowersTable = new TableQuery(tag => new FollowersTable(tag))
}