import Dependencies._
import BilbyIOBuild._

scalaVersion := "2.11.7"
name := "bilby.io"
organization in ThisBuild := "com.blueskiron"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val commonSettings = Seq(
  javaOptions += "-Xmx2G",
  fork in Test := false,
  scalaVersion in ThisBuild := "2.11.7",
  version in ThisBuild := "0.0.1"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(api, db, core, webapp)
  .dependsOn(api, db, core, webapp)
  
//play2 web app 	  
lazy val webapp = (project in file("webapp"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(api, core)
 
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  //"-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  //"-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

parallelExecution in ThisBuild := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports")