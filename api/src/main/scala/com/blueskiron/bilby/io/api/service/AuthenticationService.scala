package com.blueskiron.bilby.io.api.service

import play.api.mvc.RequestHeader
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.mvc.Result

/**
 * 
 * AtuhenticationRequest  
 * @author juri
 *
 */
trait AuthenticationService {
  
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

object AuthenticationService extends AuthenticationService