package com.blueskiron.bilby.io.core.actors

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.service.RegistrationService
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.pipe
import akka.pattern.PipeToSupport
import akka.routing.FromConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Result

object RegistrationServiceImpl extends RegistrationService {

  def authProps(authEnv: AuthenticationEnvironment): Props = Props(new RegWorker(authEnv))

  def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment) = {
    //this is counter-intuitive as workers may be created separately, prior 
    // and on different systems to the parent service
    //in this scenario (development), they are created on one system.
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "reg_w1")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "reg_w2")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "reg_w3")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "reg_w4")
    //finally -> interceptor (parent)
    system.actorOf(Props[RegistrationActor]().withDispatcher("dbio-dispatch"), name = "reg")
  }
}

class RegistrationActor extends Actor with ActorLogging {

  private val router: ActorRef = {

    context.actorOf(FromConfig.props(), "reg_router")
  }

  def receive = {
    // just route the message to the routees...
    case msg: Any => router.tell(msg, sender())
  }

}

class RegWorker(env: AuthenticationEnvironment) extends Actor {

  import RegistrationServiceImpl._
  import akka.event.Logging;
  
  val log = Logging(context.system, this)
  implicit val executionContext = context.dispatcher
  
  def receive = {
    case rc: RegistrationRequest => pipe(registerNewUser(rc)) to sender
	  case msg: Any                => sender ! ("yay! " + self.path.name + " is alive!")
  }

  private def registerNewUser(registrationRequest: RegistrationRequest): Future[AuthenticatorResult] = {
    implicit val requestHeader = registrationRequest.header
    val data = registrationRequest.data
    val user = data.user
    log.debug("registering a new user={} with email={}", user.username, data.email)
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
      outcome <- env.userService.create(user, profile.copy(avatarUrl = None))
    } yield outcome

    val authPromise = Promise[AuthenticatorResult]()
    dbAction.onComplete {
      case Success(regOut) => regOut.result match {
        case Left(user) =>  authPromise.completeWith(registerAndPublishEvents(user, linfo, registrationRequest.onSuccess))
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

  private def registerAndPublishEvents(user: User, linfo: LoginInfo, onSuccess: Result)(implicit rh: RequestHeader) = {
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

  override def postStop = env.userService.shutDown()

}