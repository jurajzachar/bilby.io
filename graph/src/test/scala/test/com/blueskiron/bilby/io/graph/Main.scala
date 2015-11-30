package test.com.blueskiron.bilby.io.graph

import com.orientechnologies.orient.server.OServerMain
import scala.io.Source

object Main extends App{
  
  val server = OServerMain.create();
  server.startup( getClass.getResourceAsStream("/orientdb-server-config.xml"))
  server.activate()
}