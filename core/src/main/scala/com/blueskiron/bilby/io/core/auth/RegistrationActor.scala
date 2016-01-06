package com.blueskiron.bilby.io.core.auth

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.ActorRef
import akka.actor.PoisonPill
import com.blueskiron.bilby.io.api.service.RegistrationService

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

class RegWorker(authEnv: AuthenticationEnvironment) extends Actor with ActorLogging with RegistrationService {

  def receive = {
    
    case RegistrationRequest(data, request) => //authEnv.userService.
    
    case msg: Any => {
      log.debug("received: {}", msg)
      sender ! ("yay! " + self.path.name + " is alive!" )
    }
  }
  
  private def saveProfile() = {
    ???
  }
  
//  private[this] def saveProfile(data: RegistrationData)(implicit request: SecuredRequest[AnyContent]) = {
//    if (request.identity.profiles.exists(_.providerID == "credentials")) {
//      throw new IllegalStateException("You're already registered.") // TODO Fix?
//    }
//
//    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
//    val authInfo = env.hasher.hash(data.password)
//    val user = request.identity.copy(
//      username = if (data.username.isEmpty) { request.identity.username } else { Some(data.username) },
//      profiles = request.identity.profiles :+ loginInfo
//    )
//    val profile = CommonSocialProfile(
//      loginInfo = loginInfo,
//      email = Some(data.email)
//    )
//    val r = Redirect(controllers.routes.HomeController.index())
//    for {
//      avatar <- env.avatarService.retrieveURL(data.email)
//      profile <- env.userService.create(user, profile.copy(avatarURL = avatar.orElse(Some("default"))))
//      u <- env.userService.save(user, update = true)
//      authInfo <- env.authInfoService.save(loginInfo, authInfo)
//      authenticator <- env.authenticatorService.create(loginInfo)
//      value <- env.authenticatorService.init(authenticator)
//      result <- env.authenticatorService.embed(value, r)
//    } yield {
//      env.eventBus.publish(SignUpEvent(u, request, request2Messages))
//      env.eventBus.publish(LoginEvent(u, request, request2Messages))
//      result
//    }
//  }
  
  override def postStop = authEnv.userService.shutDown()
}