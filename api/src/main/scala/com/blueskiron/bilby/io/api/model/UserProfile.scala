package com.blueskiron.bilby.io.api.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.impl.providers.SocialProfile
import org.joda.time.LocalDateTime

/**
 * Extension of {@link SocialProfile}
 * @author juri
 *
 */
object UserProfile {
  
/**
 *	configuration key 
 */
val defaultAvatarUrl = "default-avatar-url"

}
case class UserProfile(
  override val loginInfo: LoginInfo,
  email: Option[String],
  firstname: Option[String],
  lastname: Option[String],
  fullname: Option[String],
  avatarUrl: Option[String],
  verified: Boolean,
  created: LocalDateTime) extends SocialProfile

/**
 * AuthProvider place holder for WS that provides authentication.
 *
 */
sealed abstract class AuthProvider(val id: String)

/**
 * Supported Authentication Providers
 *
 */
object SupportedAuthProviders {
  case object NATIVE extends AuthProvider("native")
  case object FACEBOOK extends AuthProvider("facebook")
  case object GOOGLE extends AuthProvider("google")
  case object TWITTER extends AuthProvider("twitter")
  case object LINKEDIN extends AuthProvider("linkedin")
}