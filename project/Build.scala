import sbt._
import Tests._
import Keys._
import Dependencies.{ apiDeps, graphDeps, dbDeps, coreDeps }

object Build extends Build {

  lazy val api = Project(
    id = "api",
    base = file("api"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= apiDeps))

  lazy val mock = Project(
    id = "mock",
    base = file("mock"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= apiDeps)).dependsOn(api % "compile->compile")
  
  lazy val codegen = Project(
    id = "codegen",
    base = file("db/codegen"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= dbDeps
      ))
      
  lazy val db = Project(
    id = "db",
    base = file("db"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= dbDeps,
      unmanagedResourceDirectories in Compile += baseDirectory.value / "target/scala-2.11/src_managed",
      unmanagedResourceDirectories in Test += baseDirectory.value / "../mock/src/test/resources",
      sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, remove for only manual use
      slick <<= slickCodeGenTask // register manual sbt command
      )).dependsOn(api, codegen, mock % "test->test")

  // code generation task that calls the customized code generator
  lazy val slick = TaskKey[Seq[File]]("Slick Code Generation of Tables.scala")
  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  	val outputDir = (dir).getPath // place generated files in sbt's managed sources folder
  	val codegenPackage = "com.blueskiron.bilby.io.db.codegen"
    val fname = s"$outputDir/${codegenPackage.replace(".", "/")}/Tables.scala"
    println(s"\nauto-generating slick sources ...")
    println(s"output source: file://$fname\n")
  	toError(r.run(s"$codegenPackage.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
    Seq(file(fname))
  }

  lazy val graph = Project(
    id = "graph",
    base = file("graph"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= graphDeps,
      unmanagedResourceDirectories in Test += baseDirectory.value / "../mock/src/test/resources")).dependsOn(api, mock % "test->test")

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= coreDeps,
      unmanagedResourceDirectories in Test += baseDirectory.value / "../mock/src/test/resources")).dependsOn(api, db, mock % "test->test")

}
