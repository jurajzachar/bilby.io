name := "bilby.io"
scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val commonSettings = Seq(
  organization := "com.blueskiron",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.7"
)

//api-like module with no dependencies
lazy val model = (project in file("model"))
  .settings(commonSettings: _*)
  .settings(
    // other settings
  )

//persistence layer
lazy val db = (project in file("db"))
  .settings(commonSettings: _*)
  .dependsOn(model)

//actor messaging layer
lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .dependsOn(model, db)

//play2 web app 	  
lazy val webapp = (project in file("webapp"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
 .dependsOn(model, core)
 
  
lazy val root = (project in file("."))
  .dependsOn(model, db, core, webapp)
  .aggregate(model, db, core, webapp)
