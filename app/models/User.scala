package models

import scala.language.implicitConversions
import io.strongtyped.active.slick.models.Identifiable
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

case class User(
  firstName: Option[String],
  lastName: Option[String],
  userName: String,
  email: String,
  password: String,
  avatarUrl: String,
  authMethod: String,
  oAuth1Info: Option[String],
  oAuth2Info: Option[String],
  passwordInfo: Option[String], 
  userprofile_id: Option[Long] = None, //no profile defined
  visitor_id: Option[Long] = None, //no visitor defined
  id: Option[Long] = None) extends Identifiable[User] {

  /** signifies that the user is being edited **/
  /* fix-me: use account registration token */
  var verified: Boolean = false
  override type Id = Long
  override def withId(id: Id): User = copy(id = Option(id))

  override def toString(): String = {
    "--USER--\n" +
      s"first name: $firstName\n" +
      s"last name: $lastName\n" +
      s"username: $userName\n" +
      s"email: $email\n" +
      s"password: $password\n" +
      s"avatarUrl: $avatarUrl\n" +
      "------"
  }
}

case class UserProfile(
  country: Option[String],
  placeOfResidence: Option[String],
  age: Option[Short],
  id: Option[Long]) extends Identifiable[UserProfile] {

  lazy val users = Nil
  override type Id = Long
  override def withId(id: Id): UserProfile = copy(id = Option(id))

  override def toString(): String = {
    s"\tcountry: $country\n" +
      s"\tplace of residence: $placeOfResidence\n" +
      s"\tage: $age\n"
  }
}

