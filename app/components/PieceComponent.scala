package components

import java.net.URL
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.StaticQuery.staticQueryToInvoker
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.util.Try
import scala.util.Success
import models.{ Piece, PieceMetrics, User, Visitor, PieceFormInfo }
import components.JsonConversions.{ visitorReads, visitorWrites }
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Session
import scala.slick.lifted.Rep
import scala.slick.lifted.Column
import play.api.Logger
import play.utils.UriEncoding
import play.api.libs.json.Json
import play.api.cache.Cache
import models.PieceWithMetrics
import models.PieceWithMetrics
import models.PieceWithMetrics
import models.PieceWithMetrics

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

    private def processURL(str: String): URL =
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

    def popularAndRecentFirst(stream: List[(String, List[PieceWithMetrics])]) = {
      val tups = for (x <- stream; y <- x._2) yield (x._1, y)
      //tups.sortBy(x => (x._2.rating, x._2.published.get)).reverse
      tups.sortBy(x => (x._2.pieceMetrics.views.size, x._2.piece.published.get)).reverse
    }

    def fetchAll(authorId: Long): List[PieceWithMetrics] = {
      import scala.slick.driver.JdbcDriver.simple._
      DB.withSession {
        implicit session: Session =>
          //val pieces = cake.Pieces.filter(_.authorId === authorId).sortBy(_.id).list;
          val results = for {
            p <- cake.Pieces if p.authorId === authorId
            pm <- cake.PieceMets if pm.id === p.id
          } yield (p.id, p, pm)
          results.list.map(x => PieceWithMetrics(x._1, x._2, x._3))
      }
    }

    def findPublishedByUri(uri: String): (Option[PieceWithMetrics], Option[String]) = {
      EncodedPieceIdUri(uri) match {
        case (Some(id), Some(author)) => Cache.getOrElse(id.toString, 60) {
          DB.withSession {
            implicit session: Session =>
              (findPieceMetricsOptionById(id), Some(author))
          }
        }
        case (_, _) => (None, None)
      }
    }

    def findPieceMetricsOptionById(id: Long) = {
      import scala.slick.driver.JdbcDriver.simple._
      DB.withSession {
        implicit session: Session =>
          //val pieces = cake.Pieces.filter(_.authorId === authorId).sortBy(_.id).list;
          val results = for {
            p <- cake.Pieces if p.id === id
            pm <- cake.PieceMets if pm.id === p.id
          } yield (p.id, p, pm)
          results.firstOption.map(x => PieceWithMetrics(x._1, x._2, x._3))
      }
    }
    
    def findByPieceId(id: Long, authorId: Long): Piece = {
      Try(DB.withSession {
        implicit session: Session =>
          cake.Pieces.findById(id)
      }).getOrElse(Piece(draft, None, authorId, None))
    }

    def save(id: Option[Long], authorId: Long, pieceFormInfo: PieceFormInfo): Piece = {
      id match {
        //we are dealing with an unpublished new draft
        case None => {
          import scala.slick.driver.JdbcDriver.simple._
          DB.withSession {
            implicit session: Session =>
              //create piece table
              val p = Piece(pieceFormInfo, None, authorId, None).save
              //create metrics sub-table with the appropirate FK...
              PieceMets += PieceMetrics(p.id.get, Nil, 0, 0)
              p
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

    def updateVisitorMetrics(pieceId: Long, remoteAddress: String) {
      logger.debug("updating piece metrics id=${id}")
      DB.withSession {
        implicit session: Session =>
          import scala.slick.driver.JdbcDriver.simple._
          val v = Visitor(remoteAddress, System.currentTimeMillis(), None)
          val qpm = for { pm <- PieceMets if pm.id === pieceId } yield pm.views
          val serializedIds = Json.parse(qpm.firstOption.getOrElse("[]")).as[List[Long]]
          val visitors = for (id <- serializedIds) yield Visitors.findById(id)
          //filter our visitors that came from this address within the last minute 
          visitors.filter(x => x.timestamp + 60000L >= v.timestamp && x.host.equals(v.host)) match {
            //we have a unique visitor
            case Nil => {
              val id = v.save.id.get
              val updatedV = Json.toJson(id :: serializedIds).toString
              qpm.update(updatedV)
            }
            case x :: xs => //ignore repeated page loads from the same address
          }
      }
    }

  }
}