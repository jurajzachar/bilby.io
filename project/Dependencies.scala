import sbt._

object Dependencies {

  // Versions
  object Version {
    val logback = "1.1.3"
    val akka = "2.4.0"
    val slick = "3.1.0"
    val config = "1.3.0"
    val play = "2.4.4"
    val orientDbEmbedded = "0.1.0"
  }

  val logbackCore = "ch.qos.logback" % "logback-core" % Version.logback
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback
  val config = "com.typesafe" % "config" % Version.config
  
  val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4"
  val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2"
  
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  
  //val guice = "com.google.inject" % "guice" % "4.0"
  //val inject = "javax.inject" % "javax.inject" % "1"
  val scalaGuice = "net.codingwell" %% "scala-guice" % "4.0.1"
  
  val postgresql = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41" withJavadoc ()
  val slick = "com.typesafe.slick" %% "slick" % Version.slick withJavadoc ()
  
  //suport for additional postgres types with tminglei's PG driver
  val slickPgDriver = "com.blueskiron" %% "postgres-slick-driver" % "0.0.1"
  
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick withJavadoc ()
  val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % Version.slick
  val activeSlick = "io.strongtyped" %% "active-slick" % "0.3.3"
  
  val shapeless = "com.chuusai" %% "shapeless" % "2.2.5"
  val playJson = "com.typesafe.play" %% "play-json" % Version.play
  val orientDbEmbedded = "com.blueskiron" %% "orientdb-embedded" % "2.1.6"
  val gremlinScala = "com.michaelpollmeier" %% "gremlin-scala" % "3.1.0-incubating"
  val playSilhouette = "com.mohiva" %% "play-silhouette" % "3.0.4" //auth framework for oauth1, oauth2, and more => core!
  //webapp
  val playDeps = Seq(
		  		"org.webjars" %% "webjars-play" % "2.4.0-2",
		  		"org.webjars" % "bootstrap" % "3.3.6",
				"com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24", // Add bootstrap3 helpers and field constructors (http://play-bootstrap3.herokuapp.com/)
				"com.typesafe.play" %% "play-mailer" % "3.0.1")

  // Project deps
  val baseDeps = Seq(logbackCore, logbackClassic, config, scalaMock % Test, scalaTest % Test)
  val apiDeps = baseDeps ++ Seq(playJson, playSilhouette)
  val slickDeps = Seq(slick, slickPgDriver, slickCodegen, activeSlick, slickHikariCP) 
  val dbDeps = baseDeps ++ slickDeps ++ Seq(shapeless, scalaGuice, postgresql)
  val akkaDeps = Seq(akkaActor, akkaLog, akkaTestkit % Test)
  val graphDeps = baseDeps ++ akkaDeps ++ Seq(orientDbEmbedded, gremlinScala)
  val coreDeps = baseDeps ++ akkaDeps ++ Seq(scalaGuice, playSilhouette)
  val webappDeps = baseDeps ++ playDeps
}