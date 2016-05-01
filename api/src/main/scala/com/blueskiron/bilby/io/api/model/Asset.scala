package com.blueskiron.bilby.io.api.model

import java.net.URL
import scala.language.implicitConversions

object Asset {

  implicit def fromStringsToHashTags(xs: Set[String]) = for (tag <- xs) yield HashTag(tag)
  implicit def fromHashTagsToStrings(xs: Set[HashTag]) = for (tag <- xs) yield tag.toString
  
  implicit def flattenedAsset(
    id: Option[Long] = None,
    title: String,
    shortSummary: String,
    titleCover: String,
    published: Option[Long],
    author: Long,
    tags: Set[String],
    source: String): Asset = {
    Asset(AssetHeader(title, shortSummary,  Some(titleCover), tags, source), published, author, id)
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

  /* has plagiarised content ? */
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
  shortSummary: String,
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

case class AssetMetrics(id: Long, views: List[Long], likes: Int, dislikes: Int)

case class AssetWithMetrics(id: Long, Asset: Asset, AssetMetrics: AssetMetrics) {
  require(id == AssetMetrics.id)
}