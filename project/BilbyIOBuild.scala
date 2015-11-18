import sbt._
import Keys._
import Tests._
import Dependencies.{ apiDeps, dbDeps, coreDeps }

object BilbyIOBuild extends Build {
 
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
      
  lazy val db = Project(
    id = "db",
    base = file("db"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= dbDeps,
      unmanagedResourceDirectories in Test += baseDirectory.value / "../mock/src/test/resources",
      slickCodeGen <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
    )).dependsOn(api, mock % "test->test")

  lazy val slickCodeGen = TaskKey[Seq[File]]("Slick Code Generation of Tables.scala")

  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val dbName = "bilby_io_test"
    val userName = "play"
    val password = "play"
    val initScripts = Seq("create.sql", "populate.sql")
    val jdbcDriver = "org.postgresql.Driver"
    val url = s"jdbc:postgresql:$dbName"
    val slickDriver = "slick.driver.PostgresDriver"
    val targetPackageName = "com.blueskiron.bilby.io.db"
    val outputDir = (dir).getPath // place generated files in sbt's managed sources folder
    val fname = outputDir + s"/${targetPackageName.replace(".", "/")}/Tables.scala"
    println(s"\nauto-generating slick source for database schema at $url...")
    println(s"output source file file: file://$fname\n")
    toError(r.run("slick.codegen.SourceCodeGenerator",
      cp.files, Array(slickDriver, jdbcDriver, url, outputDir, targetPackageName, userName, password), s.log))
    Seq(file(fname))
  }
  
  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.7",
      libraryDependencies ++= coreDeps,
      unmanagedResourceDirectories in Test += baseDirectory.value / "../mock/src/test/resources"
      )
   ).dependsOn(api, db, mock % "test->test")
}