package controllers

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport

trait SilhouetteController extends Silhouette[User, CookieAuthenticator] with I18nSupport {
  
  def env: AuthenticationEnvironment
  
  implicit def fromSecuredRequestToUser[A](implicit request: SecuredRequest[A]): User = request.identity
  
  implicit def fromUserAwareRequestToUserOpt[A](implicit request: UserAwareRequest[A]): Option[User] = request.identity
}