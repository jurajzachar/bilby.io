package models
import io.strongtyped.active.slick.models.Identifiable
import java.util.Date
import play.api.libs.json.Json
import components.JsonConversions.visitorWrites

/**
 * @author juri
 */
case class Visitor(
  host: String = "unknownHost",
  timestamp: Long = System.currentTimeMillis(),
  id: Option[Long] = None) extends Identifiable[Visitor] {

  override type Id = Long
  override def withId(id: Id): Visitor = copy(id = Option(id))
  lazy val users = Nil

  override def toString(): String = Json.toJson(this).toString
  
}
    

