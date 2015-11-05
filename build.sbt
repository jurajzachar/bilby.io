import Dependencies._
import BilbyIOBuild._

name := "bilby.io"
organization in ThisBuild := "com.blueskiron"
scalaVersion in ThisBuild := "2.11.7"
version in ThisBuild := "0.0.1"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  javaOptions += "-Xmx2G",
  fork in Test := false,
  parallelExecution in Test := false
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(model, db, core, webapp)
  .dependsOn(model, db, core, webapp)
  
//play2 web app 	  
lazy val webapp = (project in file("webapp"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(model, core)
 
