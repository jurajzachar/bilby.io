package com.blueskiron.bilby.io.core.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.pattern.PipeToSupport
import com.mohiva.play.silhouette.api.util.Credentials
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.core.util.MutableLRU

class AuthWorker(env: AuthenticationEnvironment) extends Actor with ActorLogging {

  implicit val executionContext = context.dispatcher
  implicit object CredentialsOrdering extends Ordering[Credentials] {
    def compare(a: Credentials, b: Credentials) = 
      (a.identifier + a.password) compare (b.identifier + b.password)
  }
  val cache: MutableLRU[Credentials, User] = MutableLRU(1000) //TODO make me configurable
  
  def receive = {
    case credentials: Credentials => {
      pipe {
        env.credentials.authenticate(credentials).flatMap { linfo => 
          env.identityService.retrieve(linfo)  
        }
      } to sender
    }
  }
}
