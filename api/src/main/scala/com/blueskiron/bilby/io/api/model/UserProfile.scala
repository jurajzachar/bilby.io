package com.blueskiron.bilby.io.api.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.impl.providers.SocialProfile
import org.joda.time.LocalDateTime

case class UserProfile(
    override val loginInfo: LoginInfo, 
    email: Option[String], 
    firstName: Option[String], 
    lastName: Option[String], 
    fullName: Option[String], 
    avatarUrl: Option[String], 
    verified: Boolean, 
    created: LocalDateTime) extends SocialProfile