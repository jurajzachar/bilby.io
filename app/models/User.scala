package models

import io.strongtyped.active.slick.models.Identifiable

case class User(
  firstName: Option[String],
  lastName: Option[String],
  username: String,
  password: String,
  email: String,
  userprofile_id: Option[Long] = None, //no profile defined
  visitor_id: Option[Long] = None, //no visitor defined
  id: Option[Long] = None) extends Identifiable[User] {

  /** signifies that the user is being edited **/
  var mutates: Boolean = false
  val userProfile: Option[UserProfile] = None
  val visitor: Option[Visitor] = None
  override type Id = Long
  override def withId(id: Id): User = copy(id = Option(id))
}

case class UserProfile(
  country: Option[String],
  placeOfResidence: Option[String],
  age: Option[Short],
  id: Option[Long]) extends Identifiable[UserProfile] {

  lazy val users = Nil
  override type Id = Long
  override def withId(id: Id): UserProfile = copy(id = Option(id))

}

