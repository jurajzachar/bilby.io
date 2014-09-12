package service

import common.{ Parser, Error }
import java.io.File
import model.Review
import scalaz.Validation
import scala.io.Source
import java.io.FilenameFilter
import com.typesafe.config.{ Config, ConfigFactory }
import java.text.SimpleDateFormat

case class ReviewService(parser: Parser) {

  def findReviewBySlug(slug: String, directory: Option[File] = None): Option[Review] = directory.flatMap { d =>
    val file = new File(d, slug + ".md")
    if (file.exists) {
      reviewFromFile(file).toOption
    } else {
      None
    }
  }

  def reviewFromFile(file: File): Validation[Exception, Review] = {
    reviewFromMarkdown(Source.fromFile(file).getLines.toList,
      Error.unsafeOption(file.getName.substring(0, file.getName.lastIndexOf('.'))))
  }

  def reviewList(directory: Option[File] = None): List[Review] = {

    //create a FilenameFilter and override its accept-method
    val filefilter = new FilenameFilter() {
      //only accept files ending by .md
      def accept(dir: File, name: String) = name.endsWith(".md")
    }

    val files: List[File] = directory.map(d =>
      d.listFiles.filter(
        file => (!file.isDirectory && file.getName().endsWith(".md")))) map { _.toList } getOrElse (Nil)

    (files.map { file =>
      reviewFromFile(file)
    }).map { postValidation =>
      postValidation.toOption
    }.flatten.sortWith((p1, p2) => p1.createdAt.compareTo(p2.createdAt) > 0)

  }

  def reviewFromMarkdown(lines: List[String], slug: Option[String] = None): Validation[Exception, Review] =
    Error.unsafeValidation {
      val (header, content) = lines.span(l => l.trim != "---")

      val conf: Config = ConfigFactory.parseString(header.mkString("\n"))
      val formatter = new SimpleDateFormat("yyyy-MM-dd")

      Review(
        conf.getString("title"),
        parser.parse(content.tail.mkString("\n")),
        formatter.parse(conf.getString("date")),
        conf.getString("picture_url"),
        Error.unsafeOption(conf.getString("description")) map parser.parse,
        Error.unsafeOption(conf.getString("description")),
        slug,
        Error.unsafeOption(conf.getString("id")),
        conf.getString("rating").toInt
        )
    }

}