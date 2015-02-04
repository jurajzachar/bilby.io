
package components

import io.strongtyped.active.slick.ActiveSlick
import models.Piece
import models.User
import models.UserProfile
import models.Visitor

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

}
