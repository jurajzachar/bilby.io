package com.blueskiron.bilby.io.core

import akka.actor.Actor
import com.blueskiron.bilby.io.db.dao.UserDao

class TestActor extends Actor with UserDao{
  
  def receive = {
    case _ =>
  }
}