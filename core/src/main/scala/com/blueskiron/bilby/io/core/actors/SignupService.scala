package com.blueskiron.bilby.io.core.actors

import org.mindrot.jbcrypt.BCrypt

import com.blueskiron.bilby.io.api.UserService.Signup
import com.blueskiron.bilby.io.api.UserService.UnexpectedSignupError
import com.blueskiron.bilby.io.api.model.Account
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.db.dao.UserDao

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.pipe
import akka.routing.SmallestMailboxPool

class SignupWorker extends Actor with UserDao with ActorLogging {

  import context.dispatcher

  def receive = {
    case Signup(user) => {
      log.debug("Received signup request for: {}", user)
      val tupled = Account.unapply(user.account) map { _.copy(_2 = hashPassword(user.account.password)) }
      val work = tupled.map(hashed => {
        val _user = User.create(None, user.userName, (Account.apply _).tupled(hashed), user.userprofile, user.visitor)
        userDao.signupUser(_user) pipeTo sender
      })
      if(!work.isDefined) sender ! UnexpectedSignupError("failed when hashing user's password: " + user)
    }
  }
  
  private def hashPassword(passwd: String): String = BCrypt.hashpw(passwd, BCrypt.gensalt(12));

}

/**
 * Bootstrap the auth service and the associated worker actors
 */
object SignupService {

  def startOn(system: ActorSystem) = {
    system.actorOf(Props[SignupWorker].withRouter(SmallestMailboxPool(8)), name = "signupService")
  }
}