package com.blueskiron.bilby.io.api.model

object HstoreUtils {

  /**
   * Converts raw string representation of hstore data (for instance embedded in json) to Map[String, String]
   * @param raw
   */
  def hstoreMapFromString(raw: String): Map[String, String] = {
    raw.split(",").toList.map(_.split("=>") match {
      case Array(key, value) => (key.trim(), value.trim())
      case _                 => ("", "") //empty hstore
    }).foldLeft(Map[String, String]())((a, kv) => a ++ Map(kv._1 -> kv._2))
  }.filter(kv => !kv._1.isEmpty() && !kv._2.isEmpty()) //filter out garbage

/**
 * converts Map[String, String] into hstore stirng representation (e.g. "foo=>bar,alice=>bob,randy=>dandy")
 * @param data
 * @return
 */
def hstoreMapToString(data: Map[String, String]) = data.toList.map(kv => s"${kv._1}=>${kv._2}").mkString(",")
  
}