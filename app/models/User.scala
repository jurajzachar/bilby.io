package models

import scala.language.implicitConversions
import io.strongtyped.active.slick.models.Identifiable
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

/**
 * @author juri
 */
case class User(
  firstName: Option[String],
  lastName: Option[String],
  username: String,
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

  type Wrapped = (Visitor, UserProfile, User)

  /** signifies that the user is being edited **/
  /* fix-me: use account registration token */
  //var verified: Boolean = false
  override type Id = Long
  override def withId(id: Id): User = copy(id = Option(id))

  override def toString(): String = {
    "--USER--\n" +
      s"first name: ${firstName getOrElse None}\n" +
      s"last name: ${lastName getOrElse None}\n" +
      s"username: $username\n" +
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

  override type Id = Long
  override def withId(id: Id): UserProfile = copy(id = Option(id))
  override def toString(): String = {
    s"\tcountry: ${country getOrElse None}\n" +
      s"\tplace of residence: ${placeOfResidence getOrElse None}\n" +
      s"\tage: ${age getOrElse None}\n"
  }
}
