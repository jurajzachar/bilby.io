package test.com.blueskiron.bilby.io.core

import scala.concurrent.Promise
import play.api.test.FakeRequest
import play.api.mvc.Result
import play.api.mvc.Results
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.UserProfile
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationRejection
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationData
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationRequest
import com.blueskiron.bilby.io.api.AuthenticationService.AuthRequest
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationRequest
import com.mohiva.play.silhouette.api.util.Credentials
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders

object CoreTestData {

  val testPassword = "testSecret123"
  
  private lazy val fixtures = MockBilbyFixtures

  lazy val registrationRequests = fixtures.usersWithProfiles()
    .filter(x => x._2.exists { y => y.loginInfo.providerID == SupportedAuthProviders.CREDENTIALS.id })
    .map(e => buildFakeRegistrationRequest(e._1, e._2.head))
    
  lazy val authenticationRequests = registrationRequests.map(regReq => buildFakeAuthenticationRequest(regReq))
  
  def buildFakeRegistrationRequest(u: User, profile: UserProfile, method: String = "GET", route: String = "/") = {
    new RegistrationRequest {
      override val header = FakeRequest(method, route)
        .withHeaders(
          ("Date", java.time.LocalDateTime.now.toString()),
          ("Cookie", "empty"))
      override val data = RegistrationData(u, profile, profile.email.getOrElse("error@bilby.io"), testPassword)

      override val onSuccess = Results.Ok("registration onSuccess called")

      override val onFailure = (r: RegistrationRejection) => Results.BadRequest("denied because: " + r)

    }
  }

  def buildFakeAuthenticationRequest(regReq: RegistrationRequest): AuthRequest = {
    new AuthRequest {
      override val credentials = Credentials(regReq.data.email, testPassword)

      override val header = regReq.header

      override val onSuccess = Results.Ok("authentication onSuccess called")

      override val onFailure = () => Results.Ok("access denied")
    }
  }
}