package com.blueskiron.bilby.io.core.module

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient

class WSClientModule(wsClient: WSClient) extends AbstractModule with ScalaModule {
  
  override def configure = {
    bind[WSClient].toInstance(wsClient)
  }
}