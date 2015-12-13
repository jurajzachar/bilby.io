package com.blueskiron.bilby.io.db.codegen
import Config._
import com.blueskiron.postgresql.slick.Driver
import slick.profile.SqlProfile.ColumnOption
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object CustomizedCodeGenerator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) = {

    // prepare database, don't forget to adjust it according to your environment
    //    for (script <- initScripts) {
    //      val cmd = s"psql -d bilby_io_test -U play -f db/src/sql/$script"
    //      val exec = Runtime.getRuntime().exec(cmd);
    //      if (exec.waitFor() == 0) {
    //        println(s"$script finished.")
    //      }
    //    }

    // write the generated results to file
    val result = Await.ready(
      codegenFuture.map {
        generator =>
          {
            generator.writeToFile(
              "com.blueskiron.postgresql.slick.Driver", // use our customized postgres driver
              args(0),
              "com.blueskiron.bilby.io.db.codegen",
              "Tables",
              "Tables.scala")
          }
      }, 25 seconds)

    println("DEBUG: writing of sources complete: " + result.isCompleted)
  }

  lazy val db = slickProfile.api.Database.forURL(url, driver = jdbcDriver)
  // filter out desired tables
  val included = Seq(
    "oauth1_info",
    "oauth2_info",
    "openid_info",
    "password_info",
    "users",
    "requests",
    "session_info",
    "user_profiles",
    "assets",
    "reserved")

  val codegenFuture = db.run {
    Driver.defaultTables.map(_.filter(t => included contains t.name.name)).flatMap(Driver.createModelBuilder(_, false).buildModel)
  }.map { model =>
    new slick.codegen.SourceCodeGenerator(model) {
      override def Table = new Table(_) { table =>
        override def Column = new Column(_) { column =>
          // customize db type -> scala type mapping
          override def rawType: String = model.tpe match {
            //case "java.sql.Date"      => "org.joda.time.LocalDate"
            //case "java.sql.Time"      => "org.joda.time.LocalTime"
            //case "java.sql.Timestamp" => "org.joda.time.LocalDateTime"
            // currently, all types that are not built-in are mapped to `String`
            case "String" => model.options.find(_.isInstanceOf[ColumnOption.SqlType])
              .map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({
                case "hstore"   => "Map[String, String]"
                case "_text"    => "List[String]"
                case "_varchar" => "List[String]"
                case _          => "String"
              }).getOrElse("String")
            case _ => super.rawType.asInstanceOf[String]
          }
        }
      }

      // ensure to use our customized postgres driver at `import profile.simple._`
      override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
        s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object ${container} extends {
  val profile = ${profile}
} with ${container}
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: $profile
  import profile.api._
  ${indent(code)}
}
      """.trim()
      }

    }
  }

}