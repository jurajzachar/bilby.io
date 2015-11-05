import sbt._

object Dependencies {
  
  // Versions
  lazy val logbackVersion = "1.1.3"
  lazy val akkaVersion = "2.3.13"
  lazy val slickVersion = "3.0.3"
  lazy val configVersion = "1.3.0"

  // Libraries
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val config = "com.typesafe" % "config" % configVersion
  val specs2core = "org.specs2" %% "specs2-core" % "2.4.14"
  val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" 
  val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val postgresql = "org.postgresql" %	"postgresql" % "9.4-1201-jdbc41" withSources() withJavadoc()
  val slick = "com.typesafe.slick" %% "slick" % slickVersion withSources() withJavadoc()
  val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % slickVersion
  val hikariCP = "com.zaxxer" % "HikariCP" % "2.4.1"
  val activeSlick = "io.strongtyped" %% "active-slick" % "0.3.2"
  val playJson = "com.typesafe.play" %% "play-json"	% "2.4.3"

  // Projects
  val baseDeps = Seq(logbackCore, logbackClassic, config, specs2core % Test, scalaMock % Test)
  
  val modelDeps = baseDeps ++ Seq(playJson)
  
  val slickDeps = Seq(slick, slickCodegen, activeSlick, hikariCP)
  
  val dbDeps = baseDeps ++ slickDeps ++ Seq(postgresql)
  
  val coreDeps = baseDeps ++ Seq(akkaActor, akkaLog)
}