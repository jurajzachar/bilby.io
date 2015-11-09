package com.blueskiron.bilby.io.core.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.pipe
import com.blueskiron.bilby.io.db.dao.UserDao
import com.blueskiron.bilby.io.api.model.User
import akka.actor.ActorLogging
import com.blueskiron.bilby.io.api.UserService.{ AuthRequest, AuthResponse }

import scala.concurrent.Future
import akka.routing.RoundRobinPool
import org.mindrot.jbcrypt.BCrypt

class NativeAuthWorker extends Actor with UserDao with ActorLogging {

  import context.dispatcher

  def receive = {
    case AuthRequest(secret) => {
      userDao.userFromEitherUserNameOrEmail(secret._1) map { _.flatMap { user => authenticate(secret._2, user) } } map AuthResponse pipeTo sender
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

  /**
   * Bootstrap the auth service and the associated worker actors
   */
  object AuthService {

    def startOn(system: ActorSystem) = {
      system.actorOf(Props[NativeAuthWorker].withRouter(RoundRobinPool(4)), name = "authService")
    }
  }
}