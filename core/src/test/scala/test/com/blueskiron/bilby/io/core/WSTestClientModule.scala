package test.com.blueskiron.bilby.io.core

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient

class WSTestClientModule(wsClient: WSClient) extends AbstractModule with ScalaModule {
  
  override def configure = {
    bind[WSClient].toInstance(wsClient)
  }
}