package controllers

object SlickGenerator extends App {

  val slickDriver = "scala.slick.driver.PostgresDriver"
  val jdbcDriver = "org.postgresql.Driver"
  val url = "jdbc:postgresql://localhost:5432/play_dev"
  val outputFolder = "app"
  val pkg = "models.db.common"
  
  scala.slick.model.codegen.SourceCodeGenerator.main(
  Array(slickDriver, jdbcDriver, url, outputFolder, pkg)
  )

}