package models

import java.net.URL

import scala.language.implicitConversions
import scala.slick.jdbc.GetResult

import components.JsonConversions.pieceWrites
import io.strongtyped.active.slick.models.Identifiable
import play.api.libs.json.Json

object Piece {

  implicit def strings2HashTags(xs: Set[String]) = for (tag <- xs) yield HashTag(tag)
  implicit def hashTags2Strings(xs: Set[HashTag]) = for (tag <- xs) yield tag.toString
  implicit val getPieceOverviewResult = GetResult(r =>
    PieceOverview(r.<<, r.<<, r.<<, r.<<, Json.parse(r.nextString).as[Set[String]], r.<<, r.<<))

  implicit def flattenedPiece(
    id: Option[Long] = None,
    title: String,
    shortSummary: String,
    titleCover: String,
    published: Option[Long],
    author: Long,
    tags: Set[String],
    rating: Double,
    source: String): Piece = {
    Piece(PieceFormInfo(title, shortSummary, new URL(titleCover), tags, source), published, author, rating, id)
  }
}

case class Piece(
  header: PieceFormInfo,
  published: Option[Long],
  authorId: Long,
  rating: Double,
  id: Option[Long] = None) extends Identifiable[Piece] {
  require(rating >= 0.0 || rating <= 1.0)

  def isDraft: Boolean = published.isDefined
  override type Id = Long
  override def withId(id: Id): Piece = copy(id = Some(id))

  override def equals(other: Any) = other match {
    case that: Piece => this.header equals (that.header)
    case _           => false
  }

  /* is plagiarism content */
  def plagiarized(other: Any) = other match {
    case that: Piece => this.id == that.id ||
      (this.header.equals(that.header) && this.header.equals(that.header.source))
    case _ => false
  }
  override def toString = Json.toJson(this).toString
}

/** this is used to do a source-less read-only listing projection **/
case class PieceOverview(
  id: Long,
  author_id: Long,
  title: String,
  shortSummary: String,
  tags: Set[String],
  published: Option[Long],
  rating: Double)

/** this is used to bind editor form**/
case class PieceFormInfo(
  title: String,
  shortSummary: String,
  titleCoverUrl: URL,
  tags: Set[String],
  source: String) {
  override def equals(other: Any) = other match {
    case that: PieceFormInfo => this.title.equals(that.title) && this.shortSummary.equals(that.shortSummary)
    case _                   => false
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

