package models

import io.strongtyped.active.slick.models.Identifiable
import scala.language.implicitConversions
import java.net.URL

object Piece {

  implicit def strings2HashTags(xs: Set[String]) = for (tag <- xs) yield HashTag(tag)
  implicit def hashTags2Strings(xs: Set[HashTag]) = for (tag <- xs) yield tag.toString

  implicit def flattenedPiece(id: Option[Long] = None, 
      title: String, 
      shortSummary: String, 
      titleCover: String,
      published: Long, 
      author: Long, 
      tags: Set[String], 
      rating: Double, 
      source: String): Piece = {
    Piece(id, PieceHeader(title, shortSummary, new URL(titleCover), published, author, tags, rating), source)
  }
}
case class Piece(
  id: Option[Long] = None,
  header: PieceHeader,
  source: String) extends Identifiable[Piece] {

  override type Id = Long
  override def withId(id: Id): Piece = copy(id = Option(id))
  override def equals(other: Any) = other match {
    case that: Piece => this.id == that.id || (this.header.equals(that.header) && this.source.equals(that.source))
    case _           => false
  }
}

case class PieceHeader(
  title: String,
  shortSummary: String,
  titleCover: URL, 
  published: Long,
  authorId: Long,
  tags: Set[HashTag],
  rating: Double) {
  require(rating >= 0.0 || rating <= 1.0)
  override def equals(other: Any) = other match {
    case that: PieceHeader => this.title.equals(that.title) && this.shortSummary.equals(that.shortSummary)
    case _                 => false
  }
}

//case class PieceStats(id: Long, totalViews: Long, rating: Double) {
//  def getDaily: Double  = 0.0 //implement me
//  def getMonthly: Double = 0.0
//  def getWeekly: Double = 0.0
//}

case class HashTag(xs: String) {

  override def toString() = xs
}

