package views

import scala.util.Try
import play.api.libs.json.Json
import models.Piece
import components.JsonConversions._
package object utils {

  def long2date(ts: Long) = new java.util.Date(ts)

  def formatRating(rating: Double) =
    f"${rating * 100}%1.2f".reverse.dropWhile { _ == '0' }.dropWhile { _ == '.' }.reverse + "%"

  def parsePieceAndAuthor(payload: String) = {
    val json = Json.parse(payload)
    ((json \ "piece").as[Piece],
      (json \ "author").as[String])
  }
}