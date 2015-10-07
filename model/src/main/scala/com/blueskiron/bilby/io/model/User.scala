package com.blueskiron.bilby.io.model

import scala.language.implicitConversions

import play.libs.Json

/**
 * @author juri
 *
 */

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
    username: String,
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

  override def toString(): String = Json.toJson(this).toString
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
    id: Option[Long]) {

  override def toString(): String = Json.toJson(this).toString
}

case class Visitor (
  host: String = "unknownHost",
  timestamp: Long = System.currentTimeMillis(),
  id: Option[Long] = None) {

  override def toString(): String = Json.toJson(this).toString
  
}
