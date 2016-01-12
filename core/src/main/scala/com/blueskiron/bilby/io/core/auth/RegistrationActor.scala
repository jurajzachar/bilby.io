package com.blueskiron.bilby.io.core.auth

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.ActorRef
import akka.pattern.pipe
import com.blueskiron.bilby.io.api.service.RegistrationService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.i18n.I18nSupport
import com.blueskiron.bilby.io.api.model.User
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.i18n.{ Messages, MessagesApi }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders
import play.api.mvc.{ Result, AnyContent }
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import scala.concurrent.Promise
import scala.util.Try
import scala.concurrent.ExecutionContext
import com.mohiva.play.silhouette.api.SignUpEvent
import com.mohiva.play.silhouette.api.LoginEvent

object RegistrationServiceImpl extends RegistrationService {

  def authProps(authEnv: AuthenticationEnvironment, messagesApi: MessagesApi): Props = Props(new RegWorker(authEnv, messagesApi))

  def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment, messagesApi: MessagesApi) = {
    //this is counter-intuitive as workers may be created separately, prior 
    // and on different systems to the parent service
    //in this scenario (development), they are created on one system.
    system.actorOf(authProps(authEnv, messagesApi).withDispatcher("dbio-dispatch"), name = "reg_w1")
    system.actorOf(authProps(authEnv, messagesApi).withDispatcher("dbio-dispatch"), name = "reg_w2")
    //finally -> interceptor (parent)
    system.actorOf(Props[RegistrationActor], name = "reg")
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

class RegWorker(override val env: AuthenticationEnvironment, override val messagesApi: MessagesApi) extends Actor with ActorLogging
    with Silhouette[User, CookieAuthenticator]
    with I18nSupport
    with RegistrationService {

  import context.dispatcher

  def receive = {

    case rc: RegistrationRequest[_] => {
      saveProfile(rc.asInstanceOf[RegistrationRequest[SecuredRequest[AnyContent]]]) pipeTo sender
    }

    case msg: Any => {
      log.debug("received: {}", msg)
      sender ! ("yay! " + self.path.name + " is alive!")
    }
  }

  private def saveProfile(registrationRequest: RegistrationRequest[SecuredRequest[AnyContent]]): Future[AuthenticatorResult] = {
    implicit val request = registrationRequest.request.asInstanceOf[SecuredRequest[AnyContent]]
    val data = registrationRequest.data
    val linfo = LoginInfo(CredentialsProvider.ID, data.email)
    val authInfo = env.hasher.hash(data.password)
    val user = request.identity
    request.identity.copy(
      username = if (data.username.isEmpty) { request.identity.username } else { "Guest" },
      profiles = request.identity.profiles :+ linfo)
    val profile = data.profile.copy(loginInfo = linfo, email = Some(data.email))

    //1. phase: persist
    val dbOutcome = for {
      avatar <- env.avatarService.retrieveURL(data.email)
      outcome <- env.userService.create(user, profile.copy(avatarUrl = avatar))
    } yield outcome

    //2. create auth onSuccess or return result on failure
    dbOutcome.flatMap { regOutcome =>
      regOutcome.result match {
        case Left(user) => registerAndPublishEvents(user, linfo, registrationRequest.onSuccess)
        case Right(rejection) => {
          Future {
            AuthenticatorResult(registrationRequest.onFailure[RegistrationRejection]
            (rejection.asInstanceOf[RegistrationRejection]))
          }
        }
      }
    }
  }

  private def registerAndPublishEvents(user: User, linfo: LoginInfo, onSuccess: Result)(implicit request: SecuredRequest[AnyContent]) = {
    for {
      cookieAuth <- env.authenticatorService.create(linfo)
      cookie <- env.authenticatorService.init(cookieAuth)
      result <- env.authenticatorService.embed(cookie, onSuccess)
    } yield {
      env.eventBus.publish(SignUpEvent(user, request, request2Messages))
      env.eventBus.publish(LoginEvent(user, request, request2Messages))
      result
    }
  }

  override def postStop = env.userService.shutDown()

}