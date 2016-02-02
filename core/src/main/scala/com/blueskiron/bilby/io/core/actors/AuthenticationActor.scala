package com.blueskiron.bilby.io.core.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.pattern.PipeToSupport
import com.mohiva.play.silhouette.api.util.Credentials
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.core.util.MutableLRU
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Promise
import com.blueskiron.bilby.io.api.service.AuthenticationService
import play.api.mvc.RequestHeader
import com.mohiva.play.silhouette.api.LoginEvent
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.SmallestMailboxPool
import com.typesafe.config.ConfigFactory
import akka.routing.SmallestMailboxPool

object AuthenticationServiceImpl extends AuthenticationService {
 val config = ConfigFactory.load().getConfig("bilby.io.core")
  
 def authProps(authEnv: AuthenticationEnvironment): Props = {
   Props(new AuthWorker(authEnv, config.getInt("authCacheSize")))
 }
 
 def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment) = {
    system.actorOf(
        authProps(authEnv)
        .withDispatcher("dbio-dispatch")
        .withRouter(SmallestMailboxPool(config.getInt("workers"))), name = "auth")
  }
}

class AuthWorker(env: AuthenticationEnvironment, cacheSize: Int) extends Actor with ActorLogging {

  import AuthenticationServiceImpl._
  implicit val executionContext = context.dispatcher

  val cache: MutableLRU[LoginInfo, User] = MutableLRU(cacheSize)

  def receive = {
    case req: AuthRequest => {
      log.debug("received authentication request={}", req.credentials)
      pipe { authenticate(req) } to sender
    }
  }

  private[this] def authenticate(req: AuthRequest) = {
    val promise = Promise[Option[User]]()
    env.credentials.authenticate(req.credentials).flatMap { linfo =>
      log.debug("Found loginifo={}", linfo)
      if (cache.contains(linfo)) {
        cache.get(linfo).map { user =>
          createAndInitCookie(AuthenticatedUser(req, user, linfo))
          promise.success(Some(user))
        }
      } else {
        env.identityService.retrieve(linfo).map {
          case Some(user) =>
            createAndInitCookie(AuthenticatedUser(req, user, linfo))
            cache + ((linfo, user)) //update cache with the database record
            promise.success(Some(user))
          case None => promise.success(None)
        }
      }
      promise.future
    }
  }

  private[this] def createAndInitCookie(au: AuthenticatedUser) = {
    implicit val header = au.request.header
    env.authenticatorService.create(au.loginInfo).flatMap {
      authenticator =>
        env.authenticatorService.init(authenticator).flatMap { cookie =>
          env.authenticatorService.embed(cookie, au.request.onSuccess)
        }
    }
  }

  private case class AuthenticatedUser(request: AuthRequest, user: User, loginInfo: LoginInfo)
}
