package db

import scala.slick.driver.{ H2Driver, PostgresDriver }
import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import example.db.common._
import scala.slick.driver.PostgresDriver

/** Run SLICK code with multiple drivers. */
object MultiDBExample extends App {

  def run(dao: DAO, db: Database) {
    println("Using driver " + dao.driver)
    db withSession { implicit session =>
      try {
        dao.drop
      } catch {
        case t: Throwable => //ignore silenty
      }

      dao.create
      dao.insert("foo", "bar")
      println("- Value for key 'foo': " + dao.get("foo"))
      println("- Value for key 'baz': " + dao.get("baz"))
      val h = new DAOHelper(dao)
      println("- Using the helper: " +
        h.dao.getFirst(h.restrictKey("foo", dao.props)))
    }
  }

  try {
    run(new DAO(H2Driver),
      Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver"))
    run(new DAO(PostgresDriver),
      Database.forURL("jdbc:postgresql:postgres", driver = "org.postgresql.Driver"))
  } finally Util.unloadDrivers
}