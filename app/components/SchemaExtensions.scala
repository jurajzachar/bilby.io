
package components

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
  
  /* plain slick */
  lazy val Followers = FollowersTable
  lazy val PieceMets = PieceMetricsTable

  /* active slick */
  lazy val Visitors = EntityTableQuery[Visitor, VisitorsTable](tag => new VisitorsTable(tag))
  lazy val Users = EntityTableQuery[User, UsersTable](tag => new UsersTable(tag))
  lazy val UserProfiles = EntityTableQuery[UserProfile, UserProfilesTable](tag => new UserProfilesTable(tag))
  lazy val Pieces = EntityTableQuery[Piece, PiecesTable](tag => new PiecesTable(tag))

  val ddl = Visitors.ddl ++ UserProfiles.ddl ++ Users.ddl ++ Followers.ddl ++ Pieces.ddl ++ PieceMets.ddl

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

}
