<<<<<<< HEAD
name := "bilby.io"
scalaVersion := "2.11.7"
=======
name := """bilby.io"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  "com.adrianhurt" 		%% "play-bootstrap3" 	% "0.4",
  "org.webjars" 		%% "webjars-play" 		% "2.3.0-3",
  "org.postgresql" 		 % "postgresql" 		% "9.4-1201-jdbc41" withSources() withJavadoc(),
  "com.typesafe.play"  	%% "play-slick" 		% "0.8.1",
  "com.typesafe.slick" 	%% "slick" 				% "2.1.0" withSources() withJavadoc(),
  "io.strongtyped"		%% "active-slick"		% "0.2.2",
  "org.mindrot" 		%  "jbcrypt" 			% "0.3m",
  "org.scalatestplus"  	%% "play" 				% "1.1.1"	% "test",
  "com.typesafe.play" 	%% "play-mailer" 		% "2.4.0",
  cache,
  filters
)
>>>>>>> 68317917fbeff2a628d226e9da50b7cd6540753d

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
