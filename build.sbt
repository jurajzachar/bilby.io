name := """bilby.io"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  jdbc,
  "org.webjars" 		%% "webjars-play" 		% "2.3.0-3",
  "org.webjars" 		 % "jquery" 			% "2.1.3",
  "org.webjars" 		 % "bootstrap" 			% "3.3.2",
  "org.postgresql" 		 % "postgresql" 		% "9.3-1101-jdbc41" withSources() withJavadoc(),
  "com.typesafe.play"  	%% "play-slick" 		% "0.8.1",
  "com.typesafe.slick" 	%% "slick" 				% "2.1.0" withSources() withJavadoc(),
  "io.strongtyped"		%% "active-slick"		% "0.2.2",
  "org.mindrot" 		%  "jbcrypt" 			% "0.3m",
  "org.scalatestplus"  	%% "play" 				% "1.1.1"	% "test",
  "com.typesafe.play" 	%% "play-mailer" 		% "2.4.0"
  cache,
  filters
)

javaOptions in Test ++= Seq( "-Dconfig.resource=fake.conf" )

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

organization := "BlueSkiron"

description := "This is a micro-blogging polyglot pilot project."

licenses := Seq("Apache License" -> url("https://github.com/jurajzachar/bilby.io/blob/master/LICENSE"))
