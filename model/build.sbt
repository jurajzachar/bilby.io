name := "model"
scalaVersion := "2.11.7"
lazy val model = project.in(file("."))

libraryDependencies ++= Seq(
	"com.typesafe.play" %% "play-json" 						% "2.4.3",
	"org.mindrot" 		%  "jbcrypt" 						% "0.3m",
	"org.scalatest" 	% "scalatest_2.11" 					% "2.2.4" 	% "test",
	"org.scalamock" 	%% "scalamock-scalatest-support" 	% "3.2" 	% "test"
)
