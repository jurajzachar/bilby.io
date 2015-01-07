package models

import io.strongtyped.active.slick.models.Identifiable
import play.api.libs.json._
import scala.collection.SortedSet

case class Follower(userId: Long, fids: Set[Long]) extends Identifiable[Follower]{
  //flip this to Some(id) if you want ot update record...
  var id: Option[Long] = None
  override type Id = Long
  override def withId(id: Id): Follower = copy(id)
}
