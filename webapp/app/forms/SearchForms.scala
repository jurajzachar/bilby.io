package forms

import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object SearchForms {

  val navbarForm: Form[Search] = Form(
    mapping("searchToken" -> text)(Search.apply)(Search.unapply))

  class Search(val tokens: Set[String])

  object Search {
    /**
     * Extract unique set of search tokens
     * @param s
     * @return
     */
    def apply(s: String): Search = {
      //val spaceDelimited = s.split(" +").toSet //split by white spaces
      //val commaDelimited = s.split(" ?, ?").toSet
      //new Search(spaceDelimited union commaDelimited)
      new Search(Set(s))
    }

    def unapply(search: Search) = Some(search.tokens.mkString(" "))
  }
}