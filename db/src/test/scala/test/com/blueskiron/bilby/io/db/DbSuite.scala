package test.com.blueskiron.bilby.io.db

import io.strongtyped.active.slick.JdbcProfileProvider
import org.scalatest._
import slick.backend.DatabasePublisher
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import com.blueskiron.bilby.io.db.ApplicationDatabase
import scala.concurrent.Future

trait DbSuite extends ApplicationDatabase with BeforeAndAfterAll with Matchers with OptionValues with TryValues {

  self: Suite with JdbcProfileProvider =>

  import jdbcProfile.api._

  override protected def afterAll(): Unit = {
    database.close()
  }

  def query[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T = 
    awaitResult(super.runAction(dbAction))

  def commit[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T =
    awaitResult(super.runAction(dbAction.transactionally))
    
  def rollback[T](dbAction: DBIO[T])(implicit ex: ExecutionContext, timeout: FiniteDuration = 5 seconds): T =
    awaitResult(super.rollback(dbAction))

  private def awaitResult[T](dbAction: Future[T])(implicit ex: ExecutionContext, timeout: FiniteDuration): T = 
    Await.result(dbAction, timeout)

}