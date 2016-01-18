package test.com.blueskiron.bilby.io.db

import io.strongtyped.active.slick.JdbcProfileProvider
import org.scalatest._
import slick.backend.DatabasePublisher
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import com.blueskiron.bilby.io.db.ApplicationDatabase
import scala.concurrent.Future
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.testkit.DefaultTestDatabase

trait DbSuite extends DefaultTestDatabase with BeforeAndAfterAll with Matchers with OptionValues with TryValues {

  self: Suite with JdbcProfileProvider =>
  
  import jdbcProfile.api._
  
  implicit val executionContext = scala.concurrent.ExecutionContext.global
  
  override protected def afterAll(): Unit = {
    database.close()
  }
  
  def query[T](dbAction: DBIO[T])(): T = 
    awaitResult(super.runAction(dbAction))

  def commit[T](dbAction: DBIO[T]): T =
    awaitResult(super.runAction(dbAction.transactionally))
    
  def rollback[T](dbAction: DBIO[T]): T =
    awaitResult(super.rollback(dbAction))

  def awaitResult[T](dbAction: Future[T]): T = 
    Await.result(dbAction, defaultTimeout)

}