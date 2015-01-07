package components

import scala.slick.jdbc.JdbcBackend.{ Database, Session }
import play.api.db.slick._
import play.api.Play.current
import models._

trait UserComponent {
  import ActiveSlickCake.cake._
  
  val UserComponent = new UserComponent
  
  class UserComponent {

    def getUserProfileId(userProfile: UserProfile): Option[UserProfile#Id] = {
      UserProfiles.extractId(userProfile) match {
        case None => {
          DB.withSession {
            implicit session: Session =>
              userProfile.save
          }
          UserProfiles.extractId(userProfile)
        }
        case Some(id) => Some(id) //profile exists
      }
    }

    def signUpNewUser(user: User, userProfile: UserProfile, visitor: Visitor): User = {
      DB.withSession {
        implicit session: Session =>
          User(
            user.firstName,
            user.lastName,
            user.userName,
            user.email,
            user.password,
            user.avatarUrl,
            user.authMethod,
            user.oAuth1Info,
            user.oAuth2Info,
            user.passwordInfo,
            getUserProfileId(userProfile), //userprofile_id 
            Visitors.extractId(visitor) //visitor_id
            ).save
      }
    }
  }

}