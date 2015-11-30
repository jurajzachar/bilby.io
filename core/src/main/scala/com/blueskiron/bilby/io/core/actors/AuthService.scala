package com.blueskiron.bilby.io.core.actors

import org.mindrot.jbcrypt.BCrypt

import com.blueskiron.bilby.io.api.UserService.Authenticate
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.db.dao.UserDao

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.pipe
import akka.routing.SmallestMailboxPool

class NativeAuthWorker extends Actor with UserDao with ActorLogging {

  import context.dispatcher

  def receive = {
    case Authenticate(secret) => {
      userDao.userFromEitherUserNameOrEmail(secret._1) map { _.flatMap { user => authenticate(secret._2, user) } } pipeTo sender
    }
  }

  /**
   * Uses BCrypt hashing function to compare persisted hash with the provided plain text
   * @param user
   * @param secret
   * @return
   */
  def authenticate(candidate: String, user: User): Option[User] = {
    if (BCrypt.checkpw(candidate, user.account.password)) Some(user) else None
  }
}

/**
 * Bootstrap the auth service and the associated worker actors
 */
object AuthService {

  def startOn(system: ActorSystem) = {
    system.actorOf(Props[NativeAuthWorker].withRouter(SmallestMailboxPool(8)), name = "authService")
  }
}