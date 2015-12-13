package com.blueskiron.bilby.io.db

import io.strongtyped.active.slick.JdbcProfileProvider
import com.blueskiron.postgresql.slick.Driver

trait SlickPgJdbcProfileProvider extends JdbcProfileProvider {
  
  type JP = Driver
  val jdbcProfile: Driver = Driver
  
}