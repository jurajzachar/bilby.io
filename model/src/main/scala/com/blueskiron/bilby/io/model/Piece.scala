package com.blueskiron.bilby.io.model

import scala.language.implicitConversions

import JsonConversions.pieceHeaderWrites
import JsonConversions.pieceWrites
import play.api.libs.json.Json

object Piece {

  implicit def strings2HashTags(xs: Set[String]) = for (tag <- xs) yield HashTag(tag)
  implicit def hashTags2Strings(xs: Set[HashTag]) = for (tag <- xs) yield tag.toString
  //implicit val getPieceOverviewResult = GetResult(r =>
  //PieceOverview(r.<<, r.<<, r.<<, r.<<, Json.parse(r.nextString).as[Set[String]], r.<<, r.<<))

  implicit def flattenedPiece(
    id: Option[Long] = None,
    title: String,
    shortSummary: Option[String],
    titleCover: Option[String],
    published: Option[Long],
    author: Long,
    tags: Set[String],
    source: String): Piece = {
    Piece(PieceHeader(title, shortSummary, titleCover, tags, source), published, author, id)
  }
}

case class Piece(
  header: PieceHeader,
  published: Option[Long],
  authorId: Long,
  id: Option[Long] = None) {

  def isDraft: Boolean = published.isDefined

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

/** this is used to bind editor form**/
case class PieceHeader(
  title: String, 
  shortSummary: Option[String], 
  titleCoverUrl: Option[String], 
  tags: Set[String], 
  source: String) {
  override def equals(other: Any) = other match {
    case that: PieceHeader => this.title.equals(that.title) && this.shortSummary.equals(that.shortSummary)
    case _                   => false
  }
  
  override def toString = Json.toJson(this).toString
}

case class HashTag(xs: String) {
  override def toString() = xs
}

case class PieceMetrics(id: Long, views: List[Long], likes: Int, dislikes: Int)

case class PieceWithMetrics(id: Long, piece: Piece, pieceMetrics: PieceMetrics) {
  require(id == pieceMetrics.id)
}