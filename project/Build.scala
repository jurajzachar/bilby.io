import sbt._
import Keys._
import play.Project._
import com.typesafe.config._

object ApplicationBuild extends Build {

  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
  
  val appName = conf.getString("application.name")
  println("DEBUG: found application name: " + appName)
  
  val appVersion = conf.getString("application.version")
  println("DEBUG: found application version: " + conf.getString("application.version"))

  val appDependencies = Seq(
    jdbc, cache,
    "org.scalaz" %% "scalaz-core" % "6.0.4",
    "com.twitter" % "util-core_2.10" % "6.20.0",
    "org.pegdown" % "pegdown" % "1.4.1",
    "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
    "org.squeryl" % "squeryl_2.10" % "0.9.5-6",
    "org.postgresql" % "postgresql" % "9.3-1101-jdbc41",
    "org.mindrot" % "jbcrypt" % "0.3m"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
      "twitter.com" at "http://maven.twttr.com/",
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"),

    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "UTF-8")
    
    // to compile main.less and nothing else
    //lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "main.less")
    )

}