package com.blueskiron.bilby.io.db.codegen

object Config {
  // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val initScripts = Seq("drop.sql", "create.sql", "populate.sql")
  val dbName = "bilby_io_test"
  val userName = "play"
  val password = "play"
  val jdbcDriver = "org.postgresql.Driver"
  val url = s"jdbc:postgresql:$dbName"
  val slickProfile = com.blueskiron.postgresql.slick.Driver
}