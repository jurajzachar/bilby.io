package test.com.blueskiron.bilby.io.db
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import com.blueskiron.bilby.io.db.ActiveSlickRepos
import com.blueskiron.bilby.io.db.ActiveSlickRepos.VisitorRepo
import com.blueskiron.bilby.io.db.ModelImplicits._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.FlatSpec
import io.strongtyped.active.slick.JdbcProfileProvider

class TestActiveSlickRepos extends FlatSpec with PostgresSuite {

  val log = LoggerFactory.getLogger(getClass)
  val fixtures = MockBilbyFixtures
  "This test" should " have access to test database" in {
    val session = fixtures.testDatabase.createSession()
    val conn = session.conn
    assert(!conn.isClosed())
    conn.close()
    session.close()
  }

  "Visitor repo" should "support all CRUD operations" in {
    val repo = ActiveSlickRepos.VisitorRepo
    fixtures.visitors.foreach(visitor => commit(repo.save(visitor)))
    for { finalCount <- repo.count } yield {
      finalCount shouldBe fixtures.mockSize
    }
  }
}