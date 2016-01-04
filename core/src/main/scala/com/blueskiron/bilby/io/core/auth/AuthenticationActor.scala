package com.blueskiron.bilby.io.core.auth

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.ActorRef
import akka.actor.PoisonPill
import com.blueskiron.bilby.io.api.ActorMessages.CloseAndDie

object AuthenticationService {
  
  def authProps(authEnv: AuthenticationEnvironment): Props = Props(new AuthWorker(authEnv))
  
  def startOn(system: ActorSystem, authEnv: AuthenticationEnvironment) = {
    //this is counter-intuitive workers may be created separately and on different systems to the parent service
    //in this scenario (development), they are created on one system.
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "auth_w1")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "auth_w2")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "auth_w3")
    system.actorOf(authProps(authEnv).withDispatcher("dbio-dispatch"), name = "auth_w4")
    //finally interceptor (parent)
    system.actorOf(Props[AuthenticationActor], name = "auth")
  }
}

class AuthenticationActor extends Actor with ActorLogging {

  private val router: ActorRef = {

    context.actorOf(FromConfig.props(), "auth_router")
  }

  def receive = {
    case CloseAndDie => {
      router ! PoisonPill
    }
    //  case _ => sender ! ("yay! i am alive")
    case msg: Any => {
      log.debug("sending message={}", msg)
      router.tell(msg, sender())
    }
  }

}

class AuthWorker(authEnv: AuthenticationEnvironment) extends Actor with ActorLogging {

  def receive = {
    case msg: Any => {
      log.debug("received: {}", msg)
      sender ! ("yay! " + self.path.name + " is alive!" )
    }
  }

  override def postStop = authEnv.userService.shutDown()
}