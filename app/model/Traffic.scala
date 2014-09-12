package model

import org.squeryl.KeyedEntity
import org.squeryl.Query
import org.squeryl.PrimitiveTypeMode._
import collection.Iterable

//DAO that defines Visitor
object Visitor {
  import ObyvackaDB._
  import scala.language.postfixOps

  val allQuery: Query[Visitor] = from(visitorTable) {
    visitor => select(visitor) orderBy (visitor.timestamp desc)
  }

  def findAll: Iterable[Visitor] = inTransaction {
    allQuery.toList
  }

  def insert(visitor: Visitor): Visitor = inTransaction {
    visitorTable.insert(visitor)
  }
  def update(visitor: Visitor) {
    inTransaction { visitorTable.update(visitor) }
  }
}

//Visitor Entity
class Visitor(
    val id: Long,
    val host: String) extends KeyedEntity[Long] {

  val timestamp = System.currentTimeMillis();
}
    

