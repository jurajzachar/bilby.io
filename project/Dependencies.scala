import sbt._

object Dependencies {

  // Versions
  object Version {
    val logback = "1.1.3"
    val akka = "2.4.0"
    val slick = "3.1.0"
    val config = "1.3.0"
    val play = "2.4.3"
    val orientDb = "2.1.6"
  }

  // Libraries
  val logbackCore = "ch.qos.logback" % "logback-core" % Version.logback
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback
  val config = "com.typesafe" % "config" % Version.config
  //val specs2core = "org.specs2" %% "specs2-core" % "2.4.14"
  val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4"
  val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  val orientDbEnterprise = "com.orientechnologies" % "orientdb-enterprise" % Version.orientDb
  val orientDbServer = "com.orientechnologies" % "orientdb-server" % Version.orientDb
  val postgresql = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41" withSources () withJavadoc ()
  val slick = "com.typesafe.slick" %% "slick" % Version.slick withSources () withJavadoc ()
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick withSources () withJavadoc ()
  val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % Version.slick
  //val hikariCP = "com.zaxxer" % "HikariCP" % "2.4.1"
  val activeSlick = "io.strongtyped" %% "active-slick" % "0.3.3"
  val shapeless = "com.chuusai" %% "shapeless" % "2.2.5"
  //needed by scalatest html report
  val playJson = "com.typesafe.play" %% "play-json" % Version.play

  // Project deps
  val baseDeps = Seq(logbackCore, logbackClassic, config, scalaMock % Test, scalaTest % Test)

  val apiDeps = baseDeps ++ Seq(playJson)

  val slickDeps = Seq(slick, slickCodegen, activeSlick, slickHikariCP)

  val dbDeps = baseDeps ++ slickDeps ++ Seq(shapeless, postgresql)

  val graphDeps = baseDeps ++ Seq(orientDbEnterprise, orientDbServer)

  val coreDeps = baseDeps ++ Seq(akkaActor, akkaLog, akkaTestkit % Test)
}