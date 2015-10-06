package com.blueskiron.bilby.io.model
import java.util.Date
import play.api.libs.json.Json
import JsonConversions.visitorWrites

/**
 * @author juri
 */
case class Visitor (
  host: String = "unknownHost",
  timestamp: Long = System.currentTimeMillis(),
  id: Option[Long] = None) {

  lazy val users = Nil

  override def toString(): String = Json.toJson(this).toString
  
}
    

