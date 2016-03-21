package com.blueskiron.bilby.io.core.actors

import scala.concurrent.Promise

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.AuthenticationService
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.core.util.MutableLRU
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.Credentials

import AuthenticationServiceImpl.FromCache
import AuthenticationServiceImpl.ToCache
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.pipe
import akka.routing.FromConfig
import javax.inject.Singleton

class AuthenticationActor(cacheSize: Int) extends Actor with ActorLogging {

  import AuthenticationServiceImpl._

  private val router: ActorRef = context.actorOf(FromConfig.props(), "auth_router")

  val cache: MutableLRU[Credentials, User] = MutableLRU(cacheSize)

  def receive = {
    //handle cache
    case get: FromCache => {
      val credentials = get.request.credentials
      sender ! FromCache(get.request, get.sender, cache.get(credentials))
    }
    case put: ToCache => cache + ((put.key, put.value)) //update cache with the database record

    case inv: Invalidate => cache.-(inv.key) //destroy cache value if present

    //forward to auth workers everything else
    case msg: Any => router.tell(msg, sender())
  }

}

class AuthWorker(env: AuthenticationEnvironment, parent: ActorRef) extends Actor with ActorLogging {

  import AuthenticationServiceImpl._
  implicit val executionContext = context.dispatcher

  private case class AuthenticatedUser(request: AuthRequest, user: User, loginInfo: LoginInfo)

  def receive = {
    case req: AuthRequest => parent ! FromCache(req, sender, None)
    case cached: FromCache => pipe { authenticate(cached) } to cached.sender
    case msg: Any => sender ! ("yay! " + self.path.name + " is alive!")
  }

  private[this] def authenticate(cachedRequest: FromCache) = {
    val promise = Promise[Option[User]]()
    val credentials = cachedRequest.request.credentials
    val maybeUser = cachedRequest.value
    if (maybeUser.isDefined) {
      promise.success(maybeUser)
    } else {
      env.credentials.authenticate(credentials).flatMap { linfo =>
        env.identityService.retrieve(linfo).map {
          case Some(user) =>
            createAndInitCookie(AuthenticatedUser(cachedRequest.request, user, linfo))
            promise.success(Some(user))
            parent ! ToCache(credentials, user)
          case None => promise.success(None)
        }
      }
    }
    promise.future
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
}

object AuthenticationServiceImpl extends AuthenticationService {

  case class FromCache(request: AuthRequest, sender: ActorRef, value: Option[User])

  case class ToCache(key: Credentials, value: User)

  case class Invalidate(key: Credentials) //TODO: password change, etc

  override def actorName = "auth"

  def authProps(authEnv: AuthenticationEnvironment, parent: ActorRef): Props = {
    Props(new AuthWorker(authEnv, parent))
  }

  def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment) = {
    val cacheSize = config.getInt("authCacheSize")
    //(parent that holds common cache)
    val parent = system.actorOf(Props(new AuthenticationActor(cacheSize)).withDispatcher("dbio-dispatch"), name = actorName)
    val authWorkers = config.getInt(authWorkersKey)
    for (i <- 1 to authWorkers) {
      system.actorOf(authProps(authEnv, parent).withDispatcher("dbio-dispatch"), name = s"auth_w$i")
    }
    parent
  }
}
