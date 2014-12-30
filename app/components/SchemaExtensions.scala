package components

import io.strongtyped.active.slick.ActiveSlick
import scala.slick.jdbc.JdbcBackend
import scala.util.Try
import models.Player
import models.Visitor

trait SchemaExtensions {

  this: ActiveSlick with Schema =>

  import jdbcDriver.simple._

  val Players = EntityTableQuery[Player, PlayersTable](tag => new PlayersTable(tag))
  val Visitors = EntityTableQuery[Visitor, VisitorsTable](tag => new VisitorsTable(tag))

  implicit class VisitorExtenstions(val model: Visitor) extends ActiveRecord[Visitor] {
    override def table = Visitors
  }

  implicit class PlayersExtensions(val model: Player) extends ActiveRecord[Player] {
    override def table = Players
  }
}
