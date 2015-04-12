package utils

object EnglishGrammar {

  def oneOrMore(size: Int, word: String) = {
    if(size == 1) word else word + "s"
  }

}