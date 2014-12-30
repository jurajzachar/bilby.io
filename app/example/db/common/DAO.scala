package example.db.common
import scala.language.higherKinds
import scala.slick.driver.JdbcProfile

/** All database code goes into the DAO (data access object) class which
  * is parameterized by a Slick driver that implements JdbcProfile.
  */
class DAO(val schema: Schema) {
  // Import the query language features from the driver
  import schema.profile.simple._
  
  /** Drop the database schema */
  def drop(implicit session: Session) = schema.ddl.drop
  
  /** Create the database schema */
  def create(implicit session: Session) = schema.ddl.create
  
//  class Props(tag: Tag) extends Table[(String, String)](tag, "PROPS") {
//    def key = column[String]("KEY", O.PrimaryKey)
//    def value = column[String]("VALUE")
//    def * = (key, value)
//  }
//  val props = TableQuery[Props]
//
//  /** Create the database schema */
//  def create(implicit session: Session) =
//    props.ddl.create
//
//  /** Insert a key/value pair */
//  def insert(k: String, v: String)(implicit session: Session) =
//    props += (k, v)
//
//  /** Get the value for the given key */
//  def get(k: String)(implicit session: Session): Option[String] =
//    (for(p <- props if p.key === k) yield p.value).firstOption
//
//  /** Get the first element for a Query from this DAO */
//  def getFirst[M, U, C[_]](q: Query[M, U, C])(implicit s: Session) = q.first
}
