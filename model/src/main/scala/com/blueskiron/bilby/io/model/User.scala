package com.blueskiron.bilby.io.model

import scala.language.implicitConversions
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob
import scala.reflect.runtime.universe._
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
    userprofile_id: Option[Long] = None, //no profile defined
    visitor_id: Option[Long] = None, //no visitor defined
    id: Option[Long] = None) {

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
