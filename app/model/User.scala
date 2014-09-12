package model

import org.squeryl.Query
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity
import org.squeryl.dsl.OneToMany
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.squeryl.annotations.Transient

case class User(
    username: String,
    password: String,
    email: String,
    var userprofile_id: Long,
    var profile: UserProfile) extends ObyvackaDBObject {

  @Transient var edited: Boolean = false
}

case class UserProfile(
    country: String,
    city: Option[String],
    age: Option[Int]) extends ObyvackaDBObject {

  lazy val users: OneToMany[User] = ObyvackaDB.userProfileToUsers.left(this)
}

object User {

  import ObyvackaDB._
  import scala.language.postfixOps

  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  val allQuery: Query[User] = from(userTable) {
    user => select(user) orderBy (user.username)
  }

  def findAll: Iterable[User] = inTransaction {
    allQuery.toList
  }
  
  def findByCriteria(username: String, email: String, userprofile_id: Long): Query[User] = {
    from(userTable) {
      user =>
        where(
          (user.username === username) and
            (user.email === email) and
            (user.userprofile_id === userprofile_id)).select(user)
    }
  }
    
  def insert(user: User): User = inTransaction {
    userTable.insert(user)
  }
  def update(user: User) {
    val up = user.profile
    val _user = processUserProfile(up, user)
    //find user id to update
    
    inTransaction { 
      val idQuery = findByCriteria(_user.username, _user.email, _user.userprofile_id)
      _user.id = idQuery.toList.head.id
      userTable.update(_user) 
    }
  }

  def registerNewUser(user: User) {
    val up = user.profile
    inTransaction {
      val _user = processUserProfile(up, user)
      logger.debug("Username: " + _user.username)
      logger.debug("User profile: " + _user.userprofile_id)
      logger.debug("processing existing user? " + (user.edited))
      insert(_user)
    }

  }

  def processUserProfile(up: UserProfile, u: User): User = inTransaction {
    UserProfile.findByCriteria(up.country, up.city, up.age).toList match {
      case Nil =>
        UserProfile.insert(up); processUserProfile(up, u) //recursive call
      case x :: Nil =>
        u.userprofile_id = x.id; u
      case x :: xs => {
        logger.warn("More than one user profile matched! Pikcing the first one...")
        u.userprofile_id = x.id; u
      }
    }
  }
}

object UserProfile {

  import ObyvackaDB._

  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  val allQuery: Query[UserProfile] = from(userProfileTable) {
    userProfile => select(userProfile) orderBy (userProfile.id)
  }

  def findById(id: Long): Option[UserProfile] = inTransaction {
    from(userProfileTable) {
      userProfile =>
        where((userProfile.id === id)).select(userProfile)
    }.toList match {
      case Nil      => None
      case x :: Nil => Some(x)
      case x :: xs  => logger.error("Eeek! Inconsistent database!"); None
    }
  }

  def findByCriteria(country: String, city: Option[String], age: Option[Int]): Query[UserProfile] = {
    from(userProfileTable) {
      userProfile =>
        where(
          (userProfile.country === country) and
            (userProfile.age === age.?) and
            (userProfile.city === city.?)).select(userProfile)
    }
  }

  def findAll: Iterable[UserProfile] = inTransaction {
    allQuery.toList
  }

  def insert(userProfile: UserProfile): UserProfile = inTransaction {
    userProfileTable.insert(userProfile)
  }

  def update(userProfile: UserProfile) {
    inTransaction { userProfileTable.update(userProfile) }
  }
}