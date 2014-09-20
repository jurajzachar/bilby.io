// Comment to get more information during initialization
logLevel := Level.Info

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.4")

// less compiler
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

//akka
addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.3")
