package com.blueskiron.bilby.io.core.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.blueskiron.bilby.io.db.dao.UserDao
import com.blueskiron.bilby.io.api.UserService.{ SignupRequest, SignupOutcome }
import akka.actor.ActorLogging

class SignupWorker extends Actor with UserDao with ActorLogging {
  
  import context.dispatcher
  
  def receive = {
    case SignupRequest(user) => {
      log.debug("Received signup request for: {}", user)
      userDao.signupUser(user) pipeTo sender
    }
  }
  
}