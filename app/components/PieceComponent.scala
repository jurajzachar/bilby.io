package components

import models.Piece
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session
import scala.slick.jdbc.StaticQuery
import scala.slick.jdbc.SQLInterpolation
import scala.util.Try
import java.net.URL
import models.PieceFormInfo

/**
 * @author juri
 */
trait PieceComponent {

  def initComponent(cake: ActiveSlickCake = ActiveSlickCake.cake) = new PieceComponent(cake)

  lazy val dal = initComponent()

  class PieceComponent(val cake: ActiveSlickCake) {

    def processURL(str: String): URL =
      Try(new URL(str)).getOrElse(new URL("/assests/title-cover.png"))

    def draft = PieceFormInfo(
      "Title goes here",
      "Short summary",
      processURL(""),
      Set("foo", "bar"),
      "Welcome to Bilby.io editor")

    //TODO
    def isOwner(pieceId: Long, userId: Long) = true

    def findByPieceId(id: Option[Long], authorId: Long): Piece = {
      id match {
        case Some(id) =>
          DB.withSession {
            implicit session: Session =>
              cake.Pieces.findById(id)
          }
        case None => Piece(draft, None, authorId, 0.0, None)
      }

    }
  }
}