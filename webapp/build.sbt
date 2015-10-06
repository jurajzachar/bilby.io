name := "webapp"
scalaVersion := "2.11.7"

// Defines the project and its dependencies here instead of root build.sbt
lazy val model = project.in(file("../model"))
lazy val core = project.in(file("../core"))

lazy val webapp = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(model, core)

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
   jdbc,
  "com.h2database" 			%	"h2" 				% "1.4.187",
  "org.postgresql" 		 	%	"postgresql" 		% "9.4-1201-jdbc41" withSources() withJavadoc(),
  "com.typesafe.play"  		%%	"play-slick" 		% "1.0.1",
  "com.typesafe.slick" 		%% 	"slick" 			% "3.0.3" withSources() withJavadoc(),
  "com.typesafe.slick" 		%% 	"slick-codegen" 	% "3.0.1",
  "org.mindrot" 			%  	"jbcrypt" 			% "0.3m",
  "com.typesafe.play" 		%% 	"play-mailer" 		% "2.4.0",
  "com.mohiva" 				%% 	"play-silhouette" 	% "3.0.0-RC2",
  "org.webjars" 			% 	"bootstrap"			% "3.3.5",
  "com.adrianhurt" 			%% 	"play-bootstrap3" 	% "0.4.4-P24",
  cache,
  filters
 
)

routesGenerator := InjectedRoutesGenerator

// sbt-web
// pipelineStages := Seq(digest, gzip)

// JsEngineKeys.engineType := JsEngineKeys.EngineType.Node