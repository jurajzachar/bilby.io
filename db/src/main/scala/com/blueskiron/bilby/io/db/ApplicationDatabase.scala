package com.blueskiron.bilby.io.db

import io.strongtyped.active.slick.JdbcProfileProvider
import slick.backend.DatabasePublisher
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.postfixOps
import scala.util.{ Failure, Success }
import scala.concurrent.Future

/**
 * Idea borrowed from Renato's ActiveSlick Dbuite. 
 * But instead of blocking on Await calls we let the futures shine through.
 */
trait ApplicationDatabase extends JdbcProfileProvider {

  import jdbcProfile.api._
  
  val configPath = "bilby.io.db"
  
  lazy val database: jdbcProfile.backend.DatabaseDef = setupDb
  
  def setupDb: jdbcProfile.backend.DatabaseDef
    
  def runAction[T](dbAction: DBIO[T])(implicit ex: ExecutionContext): Future[T] = {
    database.run(dbAction)
  }

  def stream[T](dbAction: StreamingDBIO[T, T])(implicit ex: ExecutionContext): DatabasePublisher[T] = {
    database.stream(dbAction.transactionally)
  }

  def commit[T](dbAction: DBIO[T])(implicit ex: ExecutionContext): Future[T] = {
    runAction(dbAction.transactionally)
  }
    
  def rollback[T](dbAction: DBIO[T])(implicit ex: ExecutionContext) = {

    case class RollbackException(expected: T) extends RuntimeException("Rollback Exception")

    //dead code here!
    val markedForRollback = dbAction.flatMap { result =>
      DBIO.failed(RollbackException(result))
        .map(_ => result) // map back to T
    }.transactionally.asTry

    val finalAction =
      markedForRollback.map {
        case Success(result)                    => result
        case Failure(RollbackException(result)) => result
        case Failure(other)                     => throw other
      }

    runAction(finalAction)
  }
}