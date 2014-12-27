name := """bilby.io"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  "org.postgresql" % "postgresql" % "9.3-1101-jdbc41" withSources() withJavadoc(),
  "com.typesafe.slick" %% "slick" % "2.1.0" withSources() withJavadoc(),
   "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
  "org.scalatestplus" %% "play" % "1.1.1" % "test",
  cache,
  ws
)
