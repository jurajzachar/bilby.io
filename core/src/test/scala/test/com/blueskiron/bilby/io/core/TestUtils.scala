package test.com.blueskiron.bilby.io.core

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.UserProfile
import play.api.test.FakeRequest
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationData
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationRequest
import play.api.mvc.Result
import play.api.mvc.Results
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationRejection

object TestUtils {

  val testPassword = java.util.UUID.randomUUID.toString.substring(0, 8)

  def buildFakeRegistrationRequest(u: User, profile: UserProfile, method: String = "GET", route: String = "/") = {
    new RegistrationRequest {
      override val header = FakeRequest(method, route)
        .withHeaders(
          ("Date", java.time.LocalDateTime.now.toString()),
          ("Cookie", "empty"))
      override val data = RegistrationData(u, testPassword, profile.email.getOrElse("noemail@bilby.io"), profile)
      
      override val onSuccess = Results.Ok("on success called")
      
      override val onFailure = (r: RegistrationRejection) => Results.Ok("denied because: " + r)
    }
  }
}