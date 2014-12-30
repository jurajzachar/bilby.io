package db

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfter
import scala.slick.driver.{JdbcProfile, PostgresDriver}
import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import example.db.common.Schema
import example.db.common.Util
import java.util.UUID
import example.db.common.Tables
import example.db.common.DAO

class DAOSpec extends FlatSpec with PostgresSpec with MustMatchers with BeforeAndAfter {

  val schema = new Schema(PostgresDriver)
  val dao = new DAO(schema)
  before {
    database withDynSession {
        schema.ddl.createStatements.foreach { x => println(s"BEFORE: $x") }
    }
  }

  after {
    database withDynSession {
        schema.ddl.dropStatements.foreach { x => println(s"AFTER: $x") }
    }
    Util.unloadDrivers
  }
  
    trait Case {
      val visitor = schema.VisitorRow(Some("localhost"), Some(System.currentTimeMillis()), -99)
      
      //val visitor = (Some("localhost"), System.currentTimeMillis(), UUID.randomUUID())
      
      //val user2 = User(UUID.randomUUID(), "bar@example.com", new DateTime)
    }
  
//    "register" should "create a record in the user table" in new Case {
//      dao.register(user)
//      database withSession {
//        Q.queryNA[Int]("select count(*) from public.user").first() must be(1)
//      }
//    }
//  
//    it should "allow multiple records" in new Case {
//      dao.register(user)
//      dao.register(user2) must be(true)
//      database withSession {
//        Q.queryNA[Int]("select count(*) from public.user").first() must be(2)
//      }
//    }

  // ... more tests ...
}