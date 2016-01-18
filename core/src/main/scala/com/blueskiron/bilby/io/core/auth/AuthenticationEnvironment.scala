package com.blueskiron.bilby.io.core.auth
import javax.inject.{Singleton, Inject}
import com.mohiva.play.silhouette.api.Environment
import com.blueskiron.bilby.io.api.model.User
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorService
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorSettings
import scala.concurrent.duration._
import com.mohiva.play.silhouette.impl.util.DefaultFingerprintGenerator
import com.mohiva.play.silhouette.impl.util.BCryptPasswordHasher
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.blueskiron.bilby.io.db.service.PasswordInfoService
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.service.SessionInfoService
import com.blueskiron.bilby.io.db.service.UserService
import play.libs.Akka
import play.api.libs.ws.WSClient
import com.mohiva.play.silhouette.api.util.PlayHTTPLayer
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.api.EventBus
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class AuthenticationEnvironment @Inject() (
    val wsClient: WSClient,
    val configuration: Config,
    val userService: UserService[PostgresDatabase],
    val passwordInfoService: PasswordInfoService[PostgresDatabase],
    val sessionInfoService: SessionInfoService[PostgresDatabase]) extends Environment[User, CookieAuthenticator] {
  
  //link from to Slick's AsyncExecutor all of the services share the same...
  override implicit val executionContext: ExecutionContext = userService.executionContext
  
  private[this] val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  
  private[this] val httpLayer = new PlayHTTPLayer(wsClient)
  
  override val identityService = userService
    
  val hasher = new BCryptPasswordHasher()

  val idGenerator = new SecureRandomIDGenerator()

  val clock = Clock()
  
  override val eventBus = EventBus()

  val authInfoService = new DelegableAuthInfoRepository(passwordInfoService) //add other auth services heres (e.g. oauth1, oauth2)

  val credentials = new CredentialsProvider(authInfoService, hasher, Seq(hasher))
  
  private[this] val sap = new SocialAuthProviders(configuration, httpLayer, hasher, authInfoService, credentials, idGenerator, clock)
  
  val authProvider = new BasicAuthProvider(authInfoService, hasher, Nil)
  
  override val requestProviders = Seq(authProvider)
  
  val providersSeq = sap.providers
  
  val providersMap = sap.providers.toMap
  
  val avatarService = new GravatarService(httpLayer)
  
  override val authenticatorService = {
    val cfg = configuration.getConfig("silhouette.authenticator.cookie")
    new CookieAuthenticatorService(CookieAuthenticatorSettings(
      cookieName = cfg.getString("name"),
      cookiePath = cfg.getString("path"),
      cookieDomain = Option(cfg.getString("domain")),
      secureCookie = cfg.getBoolean("secure"),
      httpOnlyCookie = true,
      useFingerprinting = cfg.getBoolean("useFingerprinting"),
      cookieMaxAge = Option(cfg.getInt("maxAge") seconds),
      authenticatorIdleTimeout = Option(cfg.getInt("idleTimeout") seconds),
      authenticatorExpiry = (cfg.getInt("expiry") seconds)), Some(sessionInfoService), fingerprintGenerator, idGenerator, clock)
  }
}