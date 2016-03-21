package com.blueskiron.bilby.io.api

import play.api.mvc.RequestHeader
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.mvc.Result

/**
 * 
 * AtuhenticationRequest  
 * @author juri
 *
 */
trait AuthenticationService extends ConfiguredService with BackedByActorService {
  
  abstract class AuthRequest {
    
    /**
     * User Credentials 
     * @return
     */
    val credentials: Credentials
    
    /**
     * @return
     */
    val header: RequestHeader
    
    /**
     * Action to take when authentication succeeds 
     * @return
     */
    val onSuccess: Result
    
    /**
     * Action to take when authentication fails
     * @return
     */
    val onFailure: () => Result
  }
}

object AuthenticationService extends AuthenticationService {
  override val actorName = "auth"
}