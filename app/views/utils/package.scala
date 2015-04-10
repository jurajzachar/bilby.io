package views

import scala.util.Try
import play.api.libs.json.Json
import models.Piece
import components.JsonConversions._
import models.PieceMetrics
package object utils {

  def long2date(ts: Long) = new java.util.Date(ts)

  def calculateRating(pieceMetrics: PieceMetrics) = {
    val voted = (pieceMetrics.likes + pieceMetrics.dislikes).toDouble
    val rating = pieceMetrics.likes.toDouble / { if(voted == 0) 1.0 else voted }
    f"${rating * 100}%1.2f".reverse.dropWhile { _ == '0' }.dropWhile { _ == '.' }.reverse + "%"
  }
  
  def parsePieceAndAuthor(payload: String) = {
    val json = Json.parse(payload)
    ((json \ "piece").as[Piece],
      (json \ "author").as[String])
  }
}