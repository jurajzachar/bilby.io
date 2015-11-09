package com.blueskiron.bilby.io.api.model

import scala.language.implicitConversions

object Asset {

  implicit def strings2HashTags(xs: Set[String]) = for (tag <- xs) yield HashTag(tag)
  implicit def hashTags2Strings(xs: Set[HashTag]) = for (tag <- xs) yield tag.toString
  //implicit val getPieceOverviewResult = GetResult(r =>
  //PieceOverview(r.<<, r.<<, r.<<, r.<<, Json.parse(r.nextString).as[Set[String]], r.<<, r.<<))

  implicit def flattenedAsset(
    id: Option[Long] = None,
    title: String,
    shortSummary: Option[String],
    titleCover: Option[String],
    published: Option[Long],
    author: Long,
    tags: Set[String],
    source: String): Asset = {
    Asset(AssetHeader(title, shortSummary, titleCover, tags, source), published, author, id)
  }
}

case class Asset(
  header: AssetHeader,
  published: Option[Long],
  authorId: Long,
  id: Option[Long] = None) {

  def isDraft: Boolean = published.isDefined

  override def equals(other: Any) = other match {
    case that: Asset => this.header equals (that.header)
    case _           => false
  }

  /* is plagiarism content */
  def plagiarized(other: Any) = other match {
    case that: Asset => this.id == that.id ||
      (this.header.equals(that.header) && this.header.equals(that.header.source))
    case _ => false
  }
}

/** this is used to do a source-less read-only listing projection **/

/** this is used to bind editor form**/
case class AssetHeader(
  title: String, 
  shortSummary: Option[String], 
  titleCoverUrl: Option[String], 
  tags: Set[String], 
  source: String) {
  override def equals(other: Any) = other match {
    case that: AssetHeader => this.title.equals(that.title) && this.shortSummary.equals(that.shortSummary)
    case _                   => false
  }
  
}

case class HashTag(xs: String) {
  override def toString() = xs
}

case class AssetMetrics(id: Option[Long], views: Option[Set[Long]], likes: Int, dislikes: Int)

case class AssetWithMetrics(id: Option[Long], piece: Asset, pieceMetrics: AssetMetrics) {
  require(id == pieceMetrics.id)
}