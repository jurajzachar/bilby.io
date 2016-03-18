package com.blueskiron.bilby.io.core.actors

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.service.BackedByActorService
import com.blueskiron.bilby.io.api.service.ConfiguredService
import com.blueskiron.bilby.io.api.service.RegistrationService
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.pipe
import akka.routing.SmallestMailboxPool
import javax.inject.Singleton
import play.api.mvc.RequestHeader
import play.api.mvc.Result

class RegistrationActor(env: AuthenticationEnvironment) extends Actor with ActorLogging {

  import RegistrationServiceImpl._
  import akka.event.Logging;

  implicit val executionContext = context.dispatcher

  def receive = {
    case rc: RegistrationRequest => pipe(registerNewUser(rc)) to sender
    case msg: Any => sender ! ("yay! " + self.path.name + " is alive!")
  }

  private[this] def registerNewUser(registrationRequest: RegistrationRequest): Future[AuthenticatorResult] = {
    implicit val requestHeader = registrationRequest.header
    val data = registrationRequest.data
    val user = data.user
    log.debug("registering a new user={} with email={} and password={}", user.username, data.email, data.password)
    val linfo = LoginInfo(CredentialsProvider.ID, data.email)
    val authInfo = env.hasher.hash(data.password)

    user.copy(
      profiles = user.profiles :+ linfo)
    val profile = data.profile.copy(loginInfo = linfo, email = Some(data.email))

    //1. phase: persist
    val dbAction = for {
      //FIXME!
      //avatar <- env.avatarService.retrieveURL(data.email)
      //outcome <- env.userService.create(user, profile.copy(avatarUrl = avatar))
      authInfo <- env.authInfoService.save(linfo, authInfo)
      regOutcome <- env.userService.create(user, profile.copy(avatarUrl = None))

    } yield regOutcome

    val authPromise = Promise[AuthenticatorResult]()
    dbAction.onComplete {
      case Success(outcome) => outcome.result match {
        case Left(user) => authPromise.completeWith(registerAndPublishEvents(user, linfo, registrationRequest.onSuccess))
        case Right(rejection) => {
          authPromise.success(
            AuthenticatorResult(
              registrationRequest.onFailure(
                rejection.asInstanceOf[RegistrationRejection])))
        }
      }
      case Failure(t) => {
        log.debug("failed to create a new user {}", user)
        authPromise.failure(t)
      }
    }
    authPromise.future
  }

  private[this] def registerAndPublishEvents(user: User, linfo: LoginInfo, onSuccess: Result)(implicit rh: RequestHeader) = {
    implicit val executionContext = env.executionContext
    for {
      cookieAuth <- env.authenticatorService.create(linfo)
      cookie <- env.authenticatorService.init(cookieAuth)
      result <- env.authenticatorService.embed(cookie, onSuccess)
    } yield {
      //this must happen on the controller side by chaining the returned future...
      //env.eventBus.publish(SignUpEvent(user, request, request2Messages))
      //env.eventBus.publish(LoginEvent(user, request, request2Messages))
      result
    }
  }
  //FIXME
  //override def postStop = env.userService.closeDatabase()
}

object RegistrationServiceImpl extends RegistrationService with ConfiguredService with BackedByActorService {

  override def actorName = "reg"

  def regProps(authEnv: AuthenticationEnvironment): Props = Props(new RegistrationActor(authEnv))

  def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment) = {
    system.actorOf(
      regProps(authEnv)
        .withDispatcher("dbio-dispatch")
        .withRouter(SmallestMailboxPool(config.getInt(regWorkersKey))), name = actorName)
  }
}