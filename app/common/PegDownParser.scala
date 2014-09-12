package common

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

case class PegDownParser() extends Parser {

  val transformer = new PegDownProcessor(Extensions.ALL)

  def parse(content: String): String = transformer.markdownToHtml(content)

}