package models
import io.strongtyped.active.slick.models.Identifiable

//Visitor Entity
case class Visitor(host: Option[String]=Some("unknownHost"), ts: Long, id: Option[Long] = None) extends Identifiable[Visitor] {

  override type Id = Long
  override def withId(id: Id): Visitor = copy(id = Option(id))
  val timestamp = System.currentTimeMillis();
}
    

