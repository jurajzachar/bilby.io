package com.blueskiron.bilby.io.core.auth

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, HTTPLayer, IDGenerator, PasswordHasher }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.TwitterProvider
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateProvider, CookieStateSettings }
import com.mohiva.play.silhouette.impl.providers.oauth2.{ FacebookProvider, GoogleProvider }
import play.api.Configuration
import scala.concurrent.duration._
import scala.language.postfixOps
import com.typesafe.config.Config

class SocialAuthProviders(
    config: Config,
    httpLayer: HTTPLayer,
    hasher: PasswordHasher,
    authInfoService: AuthInfoRepository,
    credentials: CredentialsProvider,
    idGenerator: IDGenerator,
    clock: Clock
) {
  private[this] val oAuth1TokenSecretProvider = new CookieSecretProvider(CookieSecretSettings(
    cookieName = config.getString("silhouette.oauth1TokenSecretProvider.cookieName"),
    cookiePath = config.getString("silhouette.oauth1TokenSecretProvider.cookiePath"),
    cookieDomain = Option(config.getString("silhouette.oauth1TokenSecretProvider.cookieDomain")),
    secureCookie = config.getBoolean("silhouette.oauth1TokenSecretProvider.secureCookie"),
    httpOnlyCookie = config.getBoolean("silhouette.oauth1TokenSecretProvider.httpOnlyCookie"),
    expirationTime = (config.getInt("silhouette.oauth1TokenSecretProvider.expirationTime") seconds)), 
    clock)

  private[this] val oAuth2StateProvider = new CookieStateProvider(CookieStateSettings(
    cookieName = config.getString("silhouette.oauth2StateProvider.cookieName"),
    cookiePath = config.getString("silhouette.oauth2StateProvider.cookiePath"),
    cookieDomain = Option(config.getString("silhouette.oauth2StateProvider.cookieDomain")),
    secureCookie = config.getBoolean("silhouette.oauth2StateProvider.secureCookie"),
    httpOnlyCookie = config.getBoolean("silhouette.oauth2StateProvider.httpOnlyCookie"),
    expirationTime = config.getInt("silhouette.oauth2StateProvider.expirationTime") seconds), 
    idGenerator, clock)

  private[this] val facebookSettings = OAuth2Settings(
    authorizationURL = Option(config.getString("silhouette.facebook.authorizationUrl")),
    accessTokenURL = config.getString("silhouette.facebook.accessTokenUrl"),
    redirectURL = config.getString("silhouette.facebook.redirectURL"),
    clientID = config.getString("silhouette.facebook.clientId"),
    clientSecret = config.getString("silhouette.facebook.clientSecret"),
    scope = Option(config.getString("silhouette.facebook.scope"))
  )

  private[this] val facebook = new FacebookProvider(httpLayer, oAuth2StateProvider, facebookSettings)

  private[this] val googleSettings = OAuth2Settings(
    authorizationURL = Option(config.getString("silhouette.google.authorizationUrl")),
    accessTokenURL = config.getString("silhouette.google.accessTokenUrl"),
    redirectURL = config.getString("silhouette.google.redirectUrl"),
    clientID = config.getString("silhouette.google.clientId"),
    clientSecret = config.getString("silhouette.google.clientSecret"),
    scope = Option(config.getString("silhouette.google.scope"))
  )

  private[this] val google = new GoogleProvider(httpLayer, oAuth2StateProvider, googleSettings)

  private[this] val twitterSettings = OAuth1Settings(
    requestTokenURL = config.getString("silhouette.twitter.requestTokenUrl"),
    accessTokenURL = config.getString("silhouette.twitter.accessTokenUrl"),
    authorizationURL = config.getString("silhouette.twitter.authorizationUrl"),
    callbackURL = config.getString("silhouette.twitter.callbackUrl"),
    consumerKey = config.getString("silhouette.twitter.consumerKey"),
    consumerSecret = config.getString("silhouette.twitter.consumerSecret")
  )

  private[this] val twitter = new TwitterProvider(httpLayer, new PlayOAuth1Service(twitterSettings), oAuth1TokenSecretProvider, twitterSettings)

  val providers = Seq("credentials" -> credentials, "facebook" -> facebook, "google" -> google, "twitter" -> twitter)
}
