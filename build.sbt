import Dependencies._
import Build._
import Tests._

scalaVersion := "2.11.7"
name := "bilby.io"
organization := "com.blueskiron"


resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val commonSettings = Seq(
  javaOptions += "-Xmx2G",
  fork in Test := false,
  scalaVersion in ThisBuild := "2.11.7",
  version in ThisBuild := "0.0.1"
)

lazy val webapp = (project in file("webapp"))
  .settings(commonSettings: _*)
  .settings(Seq(
  	libraryDependencies ++= webappDeps,
  	routesGenerator := InjectedRoutesGenerator,
  	LessKeys.compress in Assets := true,
  	pipelineStages := Seq(digest, gzip),
  	includeFilter in (Assets, LessKeys.less) := "*.less"))
  .dependsOn(api, core, mock % "test->test")
  .enablePlugins(PlayScala)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(api, db, graph, core, webapp)
  .dependsOn(api, db, graph, core, webapp)

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

concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
parallelExecution in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports") //-h is for html reporting, -u is for xml reporting
