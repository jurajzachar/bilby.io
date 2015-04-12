package actors

import akka.actor.Actor
import components.PieceComponent
import play.api.Play.current
import play.api.cache.Cache
import play.api.db.slick.DB
import play.api.db.slick.Session
import akka.actor.ActorRef

/**
 * Yallara actor maintains cache. It uses EventBus to announce when cache has been updated.
 * @author juri
 *
 */
object Yallara {

  case object CacheWorld
  case class CacheUri(uri: String)

}

class Yallara(expiration: Int) extends Actor with PieceComponent with akka.actor.ActorLogging {

  import Yallara._

  def receive = {

    case CacheWorld => {
      Cache.set("world", {
        dal.popularAndRecentFirst(dal.fetchWorld)
      }, expiration)
      log.debug("Entries in the world cache: {}", Cache.get("world").get.asInstanceOf[List[Any]].size)
    }

    case CacheUri(uri) => {
      EncodedPieceIdUri(uri) match {
        case (Some(id), Some(author)) => Cache.set(id.toString, {
          DB.withSession {
            implicit session: Session =>
              (dal.findPieceMetricsOptionById(id), Some(author))
          }
        }, expiration)
        case (_, _) => (None, None)
      }
    }
  }

}