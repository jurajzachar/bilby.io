package models

import io.strongtyped.active.slick.models.Identifiable

case class Piece(
  id: Option[Long] = None,
  header: PieceHeader,
  source: Seq[String]) extends Identifiable[Piece] {

  override type Id = Long
  override def withId(id: Id): Piece = copy(id = Option(id))
}

case class PieceHeader(title: String, shortSummary: Seq[String], published: Long,
                       author: Long, tags: Set[HashTag])

//case class PieceStats(id: Long, totalViews: Long, rating: Double) {
//  def getDaily: Double  = 0.0 //implement me
//  def getMonthly: Double = 0.0
//  def getWeekly: Double = 0.0
//}

case class HashTag(xs: String)

