package model

import java.util.Date
import common.StringHelper

  case class Review (
      title: String, 
      content: String, 
      createdAt: Date, 
      picture_url: String,
      description: Option[String] = None, 
      rawDescription: Option[String] = None, 
      slug: Option[String] = None, 
      id: Option[String] = None,
      rating: Int) extends Ordered[Date] {

  def compare(that: Date): Int = {
    createdAt.compareTo(that)
  }

  def uniqueId: String = id match {
    case Some(i) => i
    case None => slug match {
      case Some(s) => s
      case None => StringHelper.slugify(title)
    }
  }
    
  def ratingAsPercent: String = {
    rating + " %"
  }
}