package components

import io.strongtyped.active.slick.{ TableQueries, Tables, Profile }
import models.Player
import models.Visitor


trait Schema { this: Tables with TableQueries with Profile =>

  import jdbcDriver.simple._

  class PlayersTable(tag: Tag) extends EntityTable[Player](tag, "PLAYER") {

    def name = column[String]("PLAYER_NAME")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, id.?) <> (Player.tupled, Player.unapply)
  }

  class VisitorsTable(tag: Tag) extends EntityTable[Visitor](tag, "visitor") {
    
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column host  */
    val host: Column[Option[String]] = column[Option[String]]("host")
    /** Database column timestamp  */
    val timestamp: Column[Long] = column[Long]("timestamp")
    /** Database column id AutoInc, PrimaryKey */

    def * = (host, timestamp, id.?) <> (Visitor.tupled, Visitor.unapply)
  }
}