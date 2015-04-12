package components

import java.net.URL
import scala.language.implicitConversions
import org.mindrot.jbcrypt.BCrypt
import models.PieceFormInfo
import models.Reserved
import models.User
import models.UserProfile
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.checked
import play.api.data.Forms.email
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.libs.json.Json
import scala.collection.SortedSet

trait FormBindings[M] {
  def form: Form[M]
}

trait PieceBindings extends FormBindings[PieceFormInfo] {
  
  val max1MSource = 1000 * 1000 //1MB
  
  def form: Form[PieceFormInfo] =
    Form(
      mapping(
        "title" -> text(minLength = 4, maxLength = 75),
        "shortSummary" -> text(maxLength = 300),
        "titleCoverUrl" -> optional(text),
        "tags" -> text, //TODO
        "source" -> text(maxLength = max1MSource))
        (bindPieceFormInfo)
        (unbindPieceFormInfo))

  def bindPieceFormInfo(
    title: String,
    shortSummary: String,
    titleCoverUrl: Option[String],
    tags: String,
    source: String): PieceFormInfo = {
    PieceFormInfo(
      title,
      shortSummary,
      new URL(titleCoverUrl.getOrElse("http://...")),
      tags.split(",").toSet, //instead of json, $("#tags").val(); yields comma delimeted strings
      source)
  }

  def unbindPieceFormInfo(p: PieceFormInfo): Option[(String, String, Option[String], String, String)] = {
    Option((
        p.title, 
        p.shortSummary, 
        Option(p.titleCoverUrl.toString()), 
        {if(p.tags.size > 0) Json.stringify(Json.toJson(SortedSet[String]() ++ p.tags)) else ""}, 
        p.source))
  }
  
}

trait UserBindings extends FormBindings[(User, UserProfile)] with UserComponent {

  type Password = (String, String)
  type UserCombo = (User, UserProfile)
  type OptionalStuff = (Option[String], Option[String], UserProfile)

  override def form: Form[(User, UserProfile)] = Form(

    // Define a mapping that will handle User values
    mapping(
      "username" -> text(minLength = 4),
      "email" -> email.verifying(
        "This email address is already registered.",
        email =>
          play.api.db.slick.DB.withTransaction {
            implicit session =>
              dal.getIdByEmail(email) match {
                case Some(id) => false
                case None     => true
              }
          }),
      // Create a tuple mapping for the password/confirm
      "password" -> tuple(
        "main" -> text(minLength = 8),
        "confirm" -> text).verifying(
          // Add an additional constraint: both passwords must match
          "Passwords don't match", Password => Password._1 == Password._2),

      // Create a mapping that will handle UserProfile values
      "profile" -> mapping(
        "firstName" -> optional(text),
        "lastName" -> optional(text),
        "country" -> optional(text),
        "city" -> optional(text),
        "age" -> optional(number(min = 18, max = 100))) // The mapping signature matches the UserProfile case class signature,
        // so we can use default apply/unapply functions here
        (bindOptional)(unbindOptional),

      "accept" -> checked("You must accept the conditions"))(bindUserCombo)(unbindUserCombo).verifying(
        "Sorry, this username is not available!",
        combo =>
          play.api.db.slick.DB.withTransaction {
            implicit session =>
              dal.getIdByUsername(combo._1.username) match {
                case Some(id) => false
                case None     => true
              }
          } && !Reserved.usernames.contains(combo._1.username)))

  def bindOptional(firstName: Option[String],
                   lastName: Option[String],
                   country: Option[String],
                   placeOfResidence: Option[String],
                   age: Option[Int]): OptionalStuff =
    (firstName, lastName, UserProfile(country, placeOfResidence, age, None))

  def unbindOptional(data: OptionalStuff): Option[(Option[String], Option[String], Option[String], Option[String], Option[Int])] =
    Option(data._1, data._2, data._3.country, data._3.placeOfResidence, data._3.age)

  def bindUserCombo(
    username: String,
    email: String,
    passwords: (String, String),
    data: OptionalStuff,
    accepted: Boolean): UserCombo =
    (User(
      data._1, //optional first name
      data._2, //optional last name
      username,
      email,
      BCrypt.hashpw(passwords._1, BCrypt.gensalt()),
      """@routes.Assets.at("imgs/avatar.png")""",
      "native",
      None,
      None,
      None,
      data._3.id,
      None, //TODO
      None),
      UserProfile(data._3.country, data._3.placeOfResidence, data._3.age, None))

  def unbindUserCombo(combo: UserCombo): Option[(String, String, Password, OptionalStuff, Boolean)] = {
    Option((combo._1.username, combo._1.email, ("", ""), (combo._1.firstName, combo._1.lastName,
      UserProfile(combo._2.country, combo._2.placeOfResidence, combo._2.age, None)), true))
  }

  /* TODO: a better way to convert Optional integer into optional short? */
  implicit def optShortToInt(i: Option[Int]): Option[Short] = {
    i match {
      case Some(number) => Some(number.asInstanceOf[Short])
      case _            => None
    }
  }

  implicit def optIntToShort(s: Option[Short]): Option[Int] = {
    s match {
      case Some(number) => Some(number.asInstanceOf[Int])
      case _            => None
    }
  }
}