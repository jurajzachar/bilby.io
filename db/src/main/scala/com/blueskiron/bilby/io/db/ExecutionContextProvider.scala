package com.blueskiron.bilby.io.db

import scala.concurrent.ExecutionContext

trait ExecutionContextProvider {
  
  implicit def executionContext: ExecutionContext
  
}