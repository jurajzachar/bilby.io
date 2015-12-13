package com.blueskiron.bilby.io.api.model

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.joda.time.LocalDateTime

case class User(
    id: Option[Long],
    username: Option[String],
    profiles: Seq[LoginInfo],
    roles: Set[Role] = Set(Role.User),
    active: Boolean,
    created: LocalDateTime) extends Identity {

  /**
   * Check if user is guest
   * @return
   */
  def isGuest = profiles.isEmpty

  /**
   * Check if user is admin
   * @return
   */
  def isAdmin = roles.contains(Role.Admin)

}

sealed trait Role extends Serializable {
  def name: String
}

object Role {
  def apply(role: String): Role = role match {
    case Admin.name    => Admin
    case User.name     => User
    case Customer.name => Customer
    case _ => Unknown
  }

  def unapply(role: Role): Option[String] = Some(role.name)

  object Admin extends Role {
    val name = "administrator"
  }

  object User extends Role {
    val name = "user"
  }

  object Customer extends Role {
    val name = "customer"
  }

  object Unknown extends Role {
    val name = "unknown"
  }
}

