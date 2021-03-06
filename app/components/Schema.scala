package components

import scala.language.postfixOps
import scala.slick.lifted.ProvenShape
import scala.slick.lifted.ProvenShape.proveShapeOf

import io.strongtyped.active.slick.Profile
import io.strongtyped.active.slick.TableQueries
import io.strongtyped.active.slick.Tables
import models.Follower
import models.Piece
import models.PieceMetrics
import models.User
import models.UserProfile
import models.Visitor
import play.api.libs.json.Json

trait Schema { this: Tables with TableQueries with Profile =>

  import jdbcDriver.simple._
  
  class PiecesTable(tag: Tag) extends EntityTable[Piece](tag, "piece") {

    /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    //title: String, shortSummary: Seq[String], published: Long, author: Long, tags: Set[HashTag], source: Seq[String]
    /** database column for title */
    val title = column[String]("title")
    /** database column for short summary (should be limited in length --> 600 characters) **/
    val shortSummary = column[String]("short_summary", O.DBType("text"), O.Nullable)
    /** titleCoverUrl **/
    val titleCover = column[String]("title_cover", O.DBType("text"), O.Nullable)
    /** database column for publishing timestamp **/
    val published = column[Option[Long]]("published", O.Nullable)
    val authorId = column[Long]("author_id")
    val tags = column[String]("tags", O.DBType("text"), O.Nullable)
    val source = column[String]("source", O.DBType("text"))
    
    def * : ProvenShape[Piece] = {
      val shapedValue = (id.?, title, shortSummary, titleCover, published, authorId, tags, source).shaped
      shapedValue.<>({
        tuple =>
          Piece.flattenedPiece(
            tuple._1,
            tuple._2,
            new String(tuple._3).map(_.toChar),
            new String(tuple._4).map(_.toChar),
            tuple._5,
            tuple._6,
            Json.parse(tuple._7).as[Set[String]],
            tuple._8)
      }, {
        (p: Piece) =>
          Some {
            (p.id,
              p.header.title,
              p.header.shortSummary,
              p.header.titleCoverUrl.toExternalForm(),
              p.published,
              p.authorId,
              Json.stringify(Json.toJson(p.header.tags)),
              p.header.source)
          }
      })
    }

    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val authorIdFk = foreignKey("author_id", authorId, UsersTable)(u => u.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  //lazy val PiecesTable = new TableQuery(tag => new PiecesTable(tag))
  lazy val PiecesTable = new TableQuery(tag => new PiecesTable(tag))

  /** Table description of Visitor. */
  class VisitorsTable(tag: Tag) extends EntityTable[Visitor](tag, "visitor") {

    /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column host */
    val host = column[String]("host")
    /** Database column timestamp */
    val timestamp = column[Long]("timestamp")
    /** Database column id AutoInc, PrimaryKey */

    def * = (host, timestamp, id.?) <> (Visitor.tupled, Visitor.unapply)

  }
  /** Collection-like TableQuery object for table User */
  lazy val VisitorsTable = new TableQuery(tag => new VisitorsTable(tag))

  /** Table description of USerProfile. */
  class UserProfilesTable(tag: Tag) extends EntityTable[UserProfile](tag, "userprofile") {

    /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column country  */
    val country = column[Option[String]]("country")
    /** Database column place of residence  */
    val placeOfResidence = column[Option[String]]("place_of_res")
    /** Database column age  */
    val age = column[Option[Short]]("age")

    def * = (country, placeOfResidence, age, id.?) <> (UserProfile.tupled, UserProfile.unapply)

  }
  /** Collection-like TableQuery object for table UsersProfile */
  lazy val UserProfilesTable = new TableQuery(tag => new UserProfilesTable(tag))

  /** Table description of table user. */
  class UsersTable(tag: Tag) extends EntityTable[User](tag, "user") {

    /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first name optional **/
    val firstName = column[Option[String]]("first_name")
    /** Database column last name optional **/
    val lastName = column[Option[String]]("last_name")
    /** Database column username  */
    val userName = column[String]("user_name")
    /** Database column email  */
    val email = column[String]("email")
    /** Database column password (encrypted) */
    val password = column[String]("password")
    /** Database column avatar url **/
    val avatarUrl = column[String]("avatar_url", O.DBType("text"), O.Nullable)
    /** Database column authentication method. **/
    val authMethod = column[String]("auth_method")
    /** Database column oAuth1Info */
    val oauth1 = column[Option[String]]("oauth1")
    /** Database column oAuth2Info */
    val oauth2 = column[Option[String]]("oauth2")
    /** Database column PasswordInfo */
    val passwordInfo = column[Option[String]]("passwordInfo")

    /** Database column userprofile_id  */
    val userprofileId = column[Long]("userprofile_id")
    /** Database column visitor_id **/
    val visitorId = column[Long]("visitor_id")

    /** Uniqueness Index over (username) (database name unique_username) */
    val index1 = index("unique_id", id, unique = true)
    val index2 = index("unique_username", userName, unique = true)

    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val userprofileFk = foreignKey("userprofile_id", userprofileId, UserProfilesTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val visitorFk = foreignKey("visitor_id", visitorId, VisitorsTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

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
            tuple._6,
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
              u.username,
              u.email,
              u.password,
              u.avatarUrl,
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
  class FollowersTable(tag: Tag) extends Table[Follower](tag, "follower") {
    /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.PrimaryKey)
    val fids = column[String]("fids", O.DBType("text"), O.Nullable)

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
            (f.id, Json.stringify(Json.toJson(f.fids)))
          }
      })
    }
  }
  /** Collection-like TableQuery object for table User */
  lazy val FollowersTable = new TableQuery(tag => new FollowersTable(tag))
  
  class PieceMetricsTable(tag: Tag) extends Table[PieceMetrics](tag, "piecemetrics") {
      /** Database column id AutoInc, PrimaryKey */
    val id = column[Long]("id", O.PrimaryKey)
    val views  = column[String]("views", O.DBType("text"), O.Nullable)
    val likes = column[Int]("likes", O.Nullable)
    val dislikes = column[Int]("dislikes", O.Nullable)
    
   /** Foreign key referencing User (database name userprofile_id) */
   lazy val pieceFk = foreignKey("id", id, PiecesTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

   def * : ProvenShape[PieceMetrics] = {
      val shapedValue = (id, views, likes, dislikes).shaped
      shapedValue.<>({
        tuple =>
          PieceMetrics.apply(tuple._1, Json.parse(tuple._2).as[List[Long]],
          tuple._3,
          tuple._4)
      }, {
        (pm: PieceMetrics) =>
          Some {
            (pm.id, Json.stringify(Json.toJson(pm.views)), pm.likes, pm.dislikes)
          }
      })
    }
  }
    /** Collection-like TableQuery object for table User */
  lazy val PieceMetricsTable = new TableQuery(tag => new PieceMetricsTable(tag))
}