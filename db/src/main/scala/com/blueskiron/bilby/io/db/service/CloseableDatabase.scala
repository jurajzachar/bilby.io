package com.blueskiron.bilby.io.db.service

import com.blueskiron.bilby.io.db.PostgresDatabase
import scala.concurrent.Future

trait ClosableDatabase[T <: PostgresDatabase] {
  
  protected def cake: T
  
  def shutDown() = cake.database.close()
  
}