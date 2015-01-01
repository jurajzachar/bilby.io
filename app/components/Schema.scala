package components

import io.strongtyped.active.slick.{ TableQueries, Tables, Profile }
import models._

trait Schema { this: Tables with TableQueries with Profile =>

  import jdbcDriver.simple._

  class PlayersTable(tag: Tag) extends EntityTable[Player](tag, "player") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("player_name")

    def * = (name, id.?) <> (Player.tupled, Player.unapply)

  }

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

  /** Table description of table userprofile. Objects of this class serve as prototypes for rows in queries. */
  class UserProfilesTable(tag: Tag) extends EntityTable[UserProfile](tag, "userprofile") {
    def * = (country, placeOfResidence, age, id.?) <> (UserProfile.tupled, UserProfile.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    //    def ? = (id.?, country, city, age).shaped.<>({ 
    //      r => import r._; _1.map(_ => UserProfile.tupled((_1.get, _2, _3, _4))) }, 
    //      (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column country  */
    val country: Column[Option[String]] = column[Option[String]]("country")
    /** Database column place of residence  */
    val placeOfResidence: Column[Option[String]] = column[Option[String]]("place_of_res")
    /** Database column age  */
    val age: Column[Option[Short]] = column[Option[Short]]("age")
  }
  /** Collection-like TableQuery object for table UsersProfile */
  lazy val UserProfilesTable = new TableQuery(tag => new UserProfilesTable(tag))

  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class UsersTable(tag: Tag) extends EntityTable[User](tag, "user") {
    def * = (firstName, lastName, username, password, email, userprofileId.?, visitorId.?, id.?) <> (User.tupled, User.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    //    def ? = (username, password, email, userprofileId.?, id.?).shaped.<>({
    //      r => import r._; _1.map(_ => User.tupled((_1, _2, _3, _4, _5.get)))
    //    },
    //      (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first name optional **/
    val firstName: Column[Option[String]] = column[Option[String]]("first_name")
    /** Database column last name optional **/
    val lastName: Column[Option[String]] = column[Option[String]]("last_name")
    /** Database column username  */
    val username: Column[String] = column[String]("username")
    /** Database column password  */
    val password: Column[String] = column[String]("password")
    /** Database column email  */
    val email: Column[String] = column[String]("email")
    /** Database column userprofile_id  */
    val userprofileId: Column[Long] = column[Long]("userprofile_id")
    /** Database column visitor_id **/
    val visitorId: Column[Long] = column[Long]("visitor_id")

    /** Uniqueness Index over (username) (database name unique_username) */
    val index1 = index("unique_username", username, unique = true)

    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val userprofileFk = foreignKey("userprofile_id", userprofileId, UserProfilesTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
   /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val visitorFk = foreignKey("visitor_id", userprofileId, VisitorsTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  
  }

  /** Collection-like TableQuery object for table User */
  lazy val UsersTable = new TableQuery(tag => new UsersTable(tag))

}