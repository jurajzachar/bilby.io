import Dependencies._
import Build._
import Tests._

scalaVersion := "2.11.7"
name := "bilby.io"
organization := "com.blueskiron"

lazy val commonSettings = Seq(
  javaOptions += "-Xmx2G",
  fork in Test := false,
  scalaVersion in ThisBuild := "2.11.7",
  version in ThisBuild := "1.0.0",
  resolvers ++=Seq(
    "Atlassian Releases" at "https://maven.atlassian.com/public",
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
    )
)

lazy val webapp = (project in file("webapp"))
  .settings(commonSettings: _*)
  .settings(Seq(
  	libraryDependencies ++= Seq(filters, cache) ++ webappDeps,
  	routesGenerator := InjectedRoutesGenerator,
  	TwirlKeys.templateImports += "com.blueskiron.bilby.io.api.model._",
  	LessKeys.compress in Assets := true,
  	pipelineStages := Seq(rjs, digest, gzip),
  	RjsKeys.mainModule := "bilby",
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

resolvers += Resolver.mavenLocal
concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
parallelExecution in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports") //-h is for html reporting, -u is for xml reporting
