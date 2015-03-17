package components

import java.net.URL
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.StaticQuery.staticQueryToInvoker
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.util.Try
import models.Piece
import models.Piece._
import models.PieceFormInfo
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session
import scala.slick.lifted.Rep
import scala.slick.lifted.Column
import models.PieceOverview
import play.api.Logger

/**
 * @author juri
 */
trait PieceComponent {

  def initComponent(cake: ActiveSlickCake = ActiveSlickCake.cake) = new PieceComponent(cake)

  lazy val dal = initComponent()

  class PieceComponent(val cake: ActiveSlickCake) {

    import cake._
    val logger = Logger(this.getClass)
    
    def processURL(str: String): URL =
      Try(new URL(str)).getOrElse(new URL("http://"))

    def draft = PieceFormInfo(
      "", //blank title
      "", //blank shortSummary
      //processURL(null),
      new URL("http://link-to/image.png"),
      Set(),
      "__Your Title Goes Here__ \n\n >Welcome to Bilby.io editor!")

    def isOwner(pieceId: Long, authorId: Long) = {
      DB.withSession {
        implicit session: Session =>
          sql"""select case when author_id = $authorId then true else false 
            end as is_owner from piece where id = $pieceId """.as[Boolean].first
      }
    }

    /** use PieceOverviews to minimize memory footprint **/
    def listAll(authorId: Long) = {
      DB.withSession {
        implicit session: Session =>
          Q.queryNA[PieceOverview](s"""select id, 
                        author_id, 
                        title, 
                        short_summary, 
                        tags, 
                        published, 
                        rating from piece where author_id = $authorId""").list
      }
    }

    def findByPieceId(id: Long, authorId: Long): Piece = {
      Try(DB.withSession {
        implicit session: Session =>
          cake.Pieces.findById(id)
      }).getOrElse(Piece(draft, None, authorId, 0.0, None))
    }

    def save(id: Option[Long], authorId: Long, pieceFormInfo: PieceFormInfo): Piece = {
      id match {
        //we are dealing with an unpublished new draft
        case None => {
          DB.withSession {
            implicit session: Session =>
              Piece(pieceFormInfo, None, authorId, 0.0, None).save
          }
        }
        //we are dealing with and existing piece, 
        //which may or may not have been published yet...TODO
        case Some(x) if (isOwner(x, authorId)) => {
          DB.withSession {
            implicit session: Session =>
              Pieces.findById(x).fromUpdatedHeader(pieceFormInfo).update
          }
        }
      }
    }

    def delete(id: Long) {
      logger.debug("deleting piece id=${id}")
      DB.withSession {
        implicit session: Session =>
          Pieces.tryDeleteById(id)
      }
    }
  }
}