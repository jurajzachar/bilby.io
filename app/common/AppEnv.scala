package common

import java.io.File
import play.api.Configuration
import service.ReviewService

class AppEnv(configuration: Configuration) {
	
  def dataReviewDirectory: File = new File("data/reviews/")
  def metaFile: File = new File("data/obyvacka.meta")
  def parser: PegDownParser = PegDownParser()
  def reviewService: ReviewService = new ReviewService(parser)

  private def conf(key: String): Option[String] = configuration getString key
}
