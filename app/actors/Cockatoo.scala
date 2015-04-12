package actors

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Actor

object Cockatoo {
  def props(out: ActorRef) = Props(new Cockatoo(out))
}

class Cockatoo(out: ActorRef) extends Actor with akka.actor.ActorLogging {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
  
  override def postStop() = {
    //to-do
    log.debug("post-stop called...")
  }
}