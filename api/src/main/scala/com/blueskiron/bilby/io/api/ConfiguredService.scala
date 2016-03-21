package com.blueskiron.bilby.io.api

import com.typesafe.config.ConfigFactory

trait ConfiguredService {

  private lazy val _config = ConfigFactory.load().getConfig("bilby.io.core")

  /**
   * number of authenticated user entries that are held in memory
   */
  val authCacheSizeKey = "authCacheSize"
  
  /**
   * number of authentication worker actors
   */
  val authWorkersKey = "authWorkers"
  
  /**
   * number of registration worker actors
   */
  val regWorkersKey = "regWorkers"

  /**
   * @return loaded Config
   */
  def config = _config

}