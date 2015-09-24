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

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javaOptions in Test ++= Seq( "-Dconfig.resource=fake.conf" )

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

organization := "BlueSkiron"

description := "This is a micro-blogging polyglot pilot project."

licenses := Seq("Apache License" -> url("https://github.com/jurajzachar/bilby.io/blob/master/LICENSE"))
