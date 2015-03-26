package components

import java.net.URL
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.StaticQuery.staticQueryToInvoker
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.util.Try
import scala.util.Success
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
import play.utils.UriEncoding
import models.User

/**
 * @author juri
 */
trait PieceComponent {

  case object EncodedPieceIdUri {
    //Uri encoded id-user-title
    private val uriF = "%d-%s-%s"
    private val uriR = """(\d+)-(\w+)-""".r //capture id and author's username

    def unapply(id: Long, authorUserName: String, title: String) =
      uriF.format(id, authorUserName, UriEncoding.encodePathSegment(title, "UTF-8"))

    def apply(encodedPieceIdUri: String): (Option[Long], Option[String]) = {
      uriR.findFirstMatchIn(encodedPieceIdUri) match {
        case Some(matches) => {
          matches.subgroups match {
            case Nil => (None, None) //invalid
            case id :: author => (Try(java.lang.Long.parseLong(id)), Try(author.head)) match {
              case (Success(id), Success(author)) => (Some(id), Some(author))
              case _                              => (None, None)
            }
          }
        }
        case None => (None, None)
      }
    }
  }

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
    
    def fetchAll(authorId: Long): List[Piece] = {
      import scala.slick.driver.JdbcDriver.simple._
      DB.withSession {
        implicit session: Session =>
          cake.Pieces.filter(_.authorId === authorId).sortBy(_.id).list;
      }
    }
    
    /** TODO: use PieceOverviews to minimize memory footprint FIXME: how to get templates work wtih ajax load?**/
    def fetchAllOverviews(authorId: Long) = {
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
    
    def findByUri(uri: String): (Option[Piece], Option[String]) = {
      EncodedPieceIdUri(uri) match {
        case (Some(id), Some(author)) => DB.withSession {
          implicit session: Session =>
            (cake.Pieces.findOptionById(id), Some(author))
        }
        case (_,_) => (None, None)
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
        //which may or may not have been published
        case Some(x) if (isOwner(x, authorId)) => {
          DB.withSession {
            implicit session: Session =>
              Pieces.findById(x).copy(pieceFormInfo).update
          }
        }
      }
    }
    
    def publish(id: Long) = {
      DB.withSession {
        implicit session: Session =>
          Pieces.findById(id).copy(
            published = Some(System.currentTimeMillis())).update.id.get
      }
    }

    def unpublish(id: Long) = {
      DB.withSession {
        implicit session: Session =>
          Pieces.findById(id).copy(
            published = None).update.id.get
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