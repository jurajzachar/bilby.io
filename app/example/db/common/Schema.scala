package example.db.common

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Schema extends {
  val profile = scala.slick.driver.PostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = User.ddl ++ Userprofile.ddl ++ Visitor.ddl
  
  /** Entity class storing rows of table User
   *  @param id Database column id AutoInc, PrimaryKey
   *  @param username Database column username 
   *  @param password Database column password 
   *  @param email Database column email 
   *  @param userprofileId Database column userprofile_id  */
  case class UserRow(id: Long, username: Option[String], password: Option[String], email: Option[String], userprofileId: Long)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[Option[String]]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Long], <<?[String], <<?[String], <<?[String], <<[Long]))
  }
  
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "user") {
    def * = (id, username, password, email, userprofileId) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, username, password, email, userprofileId.?).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username  */
    val username: Column[Option[String]] = column[Option[String]]("username")
    /** Database column password  */
    val password: Column[Option[String]] = column[Option[String]]("password")
    /** Database column email  */
    val email: Column[Option[String]] = column[Option[String]]("email")
    /** Database column userprofile_id  */
    val userprofileId: Column[Long] = column[Long]("userprofile_id")
    
    /** Foreign key referencing Userprofile (database name userprofile_id) */
    lazy val userprofileFk = foreignKey("userprofile_id", userprofileId, Userprofile)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    
    /** Uniqueness Index over (username) (database name unique_username) */
    val index1 = index("unique_username", username, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
  
  /** Entity class storing rows of table Userprofile
   *  @param id Database column id AutoInc, PrimaryKey
   *  @param country Database column country 
   *  @param city Database column city 
   *  @param age Database column age  */
  case class UserprofileRow(id: Long, country: Option[String], city: Option[String], age: Option[Short])
  /** GetResult implicit for fetching UserprofileRow objects using plain SQL queries */
  implicit def GetResultUserprofileRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[Short]]): GR[UserprofileRow] = GR{
    prs => import prs._
    UserprofileRow.tupled((<<[Long], <<?[String], <<?[String], <<?[Short]))
  }
  
  /** Table description of table userprofile. Objects of this class serve as prototypes for rows in queries. */
  class Userprofile(_tableTag: Tag) extends Table[UserprofileRow](_tableTag, "userprofile") {
    def * = (id, country, city, age) <> (UserprofileRow.tupled, UserprofileRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, country, city, age).shaped.<>({r=>import r._; _1.map(_=> UserprofileRow.tupled((_1.get, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column country  */
    val country: Column[Option[String]] = column[Option[String]]("country")
    /** Database column city  */
    val city: Column[Option[String]] = column[Option[String]]("city")
    /** Database column age  */
    val age: Column[Option[Short]] = column[Option[Short]]("age")
  }
  /** Collection-like TableQuery object for table Userprofile */
  lazy val Userprofile = new TableQuery(tag => new Userprofile(tag))
  
  /** Entity class storing rows of table Visitor
   *  @param host Database column host 
   *  @param timestamp Database column timestamp 
   *  @param id Database column id AutoInc, PrimaryKey */
  case class VisitorRow(host: Option[String], timestamp: Option[Long], id: Int)
  /** GetResult implicit for fetching VisitorRow objects using plain SQL queries */
  implicit def GetResultVisitorRow(implicit e0: GR[Option[String]], e1: GR[Option[Long]], e2: GR[Int]): GR[VisitorRow] = GR{
    prs => import prs._
    VisitorRow.tupled((<<?[String], <<?[Long], <<[Int]))
  }
  
  /** Table description of table visitor. Objects of this class serve as prototypes for rows in queries. */
  class Visitor(_tableTag: Tag) extends Table[VisitorRow](_tableTag, "visitor") {
    def * = (host, timestamp, id) <> (VisitorRow.tupled, VisitorRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (host, timestamp, id.?).shaped.<>({r=>import r._; _3.map(_=> VisitorRow.tupled((_1, _2, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column host  */
    val host: Column[Option[String]] = column[Option[String]]("host")
    /** Database column timestamp  */
    val timestamp: Column[Option[Long]] = column[Option[Long]]("timestamp")
    /** Database column id AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table Visitor */
  lazy val Visitor = new TableQuery(tag => new Visitor(tag))
}