package db

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfter

class PostgresUserDaoSpec extends FlatSpec with PostgresSpec with MustMatchers with BeforeAndAfter {

  import example.db.common.Schema._

  before {
    database withSession {
      implicit session =>
        ddl.createStatements.foreach { x => println(s"BEFORE: $x") }
    }
  }

  after {
    database withSession {
      implicit session =>
        ddl.dropStatements.foreach { x => println(s"AFTER: $x") }
    }
  }
  //
  //  trait Case {
  //    val dao = new PostgresUserDao with DatabaseProvider {
  //      val db = database
  //    }
  //    val user = User(UUID.randomUUID(), "foo@example.com", new DateTime)
  //    val user2 = User(UUID.randomUUID(), "bar@example.com", new DateTime)
  //  }
  //
  //  "register" should "create a record in the user table" in new Case {
  //    dao.register(user)
  //    database withSession {
  //      Q.queryNA[Int]("select count(*) from public.user").first() must be(1)
  //    }
  //  }
  //
  //  it should "allow multiple records" in new Case {
  //    dao.register(user)
  //    dao.register(user2) must be(true)
  //    database withSession {
  //      Q.queryNA[Int]("select count(*) from public.user").first() must be(2)
  //    }
  //  }

  // ... more tests ...
}