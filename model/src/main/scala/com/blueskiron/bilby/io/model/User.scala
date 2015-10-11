package com.blueskiron.bilby.io.model

import scala.language.implicitConversions

/**
 * @author juri
 *
 */

object User {
  
  def userWithProfileAndVisitor(u: User, up: Option[UserProfile], v: Option[Visitor]) = {
    User(u.firstName, u.lastName, u.userName, u.email, u.password, u.avatarUrl, u.authMethod, u.oAuth1Info, u.oAuth2Info, u.passwordInfo, up, v, u.id)
  }
  
}

/**
 * @param firstName
 * @param lastName
 * @param username
 * @param email
 * @param password
 * @param avatarUrl
 * @param authMethod
 * @param oAuth1Info
 * @param oAuth2Info
 * @param passwordInfo
 * @param userprofile_id
 * @param visitor_id
 * @param id
 */
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
    userprofile: Option[UserProfile] = None, //no profile defined
    visitor: Option[Visitor] = None, //no visitor defined
    id: Option[Long] = None) { //new user

}

/**
 * @param country
 * @param placeOfResidence
 * @param age
 * @param id
 */
case class UserProfile(
    country: Option[String],
    placeOfResidence: Option[String],
    age: Option[Short],
    id: Option[Long] = None) {

}

case class Visitor (
  host: String = "unknownHost",
  timestamp: Long = System.currentTimeMillis(),
  id: Option[Long] = None) {
}
