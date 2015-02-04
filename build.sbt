name := """bilby.io"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  jdbc,
  "org.postgresql" 		 % "postgresql" 		% "9.3-1101-jdbc41" withSources() withJavadoc(),
  "com.typesafe.play"  	%% "play-slick" 		% "0.8.1",
  "com.typesafe.slick" 	%% "slick" 				% "2.1.0" withSources() withJavadoc(),
  "com.typesafe.slick" 	%% "slick-codegen" 		% "2.1.0",
  "io.strongtyped"		%% "active-slick"		% "0.2.2",
  "org.mindrot" 		%  "jbcrypt" 			% "0.3m",
  "org.scalatestplus"  	%% "play" 				% "1.1.1"	% "test",
  cache,
  ws
)

javaOptions in Test ++= Seq( "-Dconfig.resource=fake.conf" )

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false
