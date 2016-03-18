package com.blueskiron.bilby.io.api.service

trait BackedByActorService {
  /**
   * top level unique actor name that backs this configured service
   */
  def actorName: String

}