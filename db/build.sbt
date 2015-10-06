name := "db"
scalaVersion := "2.11.7"

lazy val model = project.in(file("../model"))

lazy val db = project.in(file("."))
 .settings(Project.defaultSettings ++ Seq(
  libraryDependencies ++= Seq(
  jdbc,
  "com.h2database" 			%	"h2" 							% "1.4.187",
  "org.postgresql" 		 	%	"postgresql" 					% "9.4-1201-jdbc41" withSources() withJavadoc(),
  "com.typesafe.slick" 		%% 	"slick" 						% "3.0.3" withSources() withJavadoc(),
  "com.typesafe.slick" 		%% 	"slick-codegen" 				% "3.0.1",
  "io.strongtyped" 			%% 	"active-slick" 					% "0.3.2",
  "com.chuusai" 			%% 	"shapeless" 					% "2.2.5",
  "com.typesafe" 			% 	"config" 						% "1.3.0",
  "org.scalatest" 			% "scalatest_2.11" 					% "2.2.4" 	% "test",
  "org.scalamock" 			%% "scalamock-scalatest-support" 	% "3.2" 	% "test"
	),
   sourceGenerators in Compile <+= slickGenerate // register automatic code generation on every compile, remove for only manual use
)).dependsOn(model)

lazy val slickGenerate = taskKey[Seq[File]]("Slick Code Generation")

slickGenerate := {
  val dbName = "bilby_io"
  val userName = "play"
  val password = "play"
  val initScripts = Seq("create.sql","populate.sql")
  val jdbcDriver =  "org.postgresql.Driver"
  val url = s"jdbc:postgresql:$dbName"
  val slickDriver = "slick.driver.PostgresDriver"
  val targetPackageName = "com.blueskiron.bilby.io.db"
  val outputDir = ((sourceManaged in Compile).value).getPath // place generated files in sbt's managed sources folder
  val fname = outputDir + s"/${targetPackageName.replace(".", "/")}/Tables.scala"
  println(s"\nauto-generating slick source for database schema at $url...")
  println(s"output source file file: file://$fname\n")
  (runner in Compile).value.run("slick.codegen.SourceCodeGenerator", 
  (dependencyClasspath in Compile).value.files, Array(slickDriver, jdbcDriver, url, outputDir, targetPackageName, userName, password), streams.value.log)
  Seq(file(fname))
  
}