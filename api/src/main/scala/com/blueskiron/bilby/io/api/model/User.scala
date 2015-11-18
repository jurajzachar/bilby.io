package com.blueskiron.bilby.io.api.model

import scala.language.implicitConversions

/**
 * @author juri
 *
 */

object User {

  def create(id: Option[Long] = None, userName: String, a: Account, up: Option[UserProfile], v: Option[Visitor]) = {
    User(userName, a, up, v, id)
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
case class User(userName: String,
                account: Account,
                userprofile: Option[UserProfile] = None, //no profile defined
                visitor: Option[Visitor] = None, //no visitor defined
                id: Option[Long] = None)

/**
 * @author juri
 *
 */
case class Account(email: String,
                   password: String,
                   avatarUrl: String,
                   authMethod: String,
                   oAuth1Info: Option[String],
                   oAuth2Info: Option[String],
                   passwordInfo: Option[String],
                   verified: Boolean,
                   active: Boolean,
                   id: Option[Long])

/**
 * @param country
 * @param placeOfResidence
 * @param age
 * @param id
 */
case class UserProfile(firstName: Option[String],
                       lastName: Option[String],
                       country: Option[String],
                       placeOfRes: Option[String],
                       age: Option[Short],
                       id: Option[Long] = None)

/**
 * @param host
 * @param timestamp
 * @param id
 */
case class Visitor(host: String = "unknownHost", timestamp: Long, id: Option[Long] = None) 

/**
 * @param leads
 * @param id
 */
case class Follower(leads: Set[User], id: Option[Long] = None)

