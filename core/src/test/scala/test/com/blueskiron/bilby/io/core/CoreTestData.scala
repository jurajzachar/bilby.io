package test.com.blueskiron.bilby.io.core

import play.api.test.FakeRequest
import play.api.mvc.Result
import play.api.mvc.Results
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.UserProfile
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationRejection
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationData
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationRequest
import com.blueskiron.bilby.io.api.service.AuthenticationService.AuthRequest
import com.mohiva.play.silhouette.api.util.Credentials
import com.blueskiron.bilby.io.mock.MockBilbyFixtures
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders
import com.blueskiron.bilby.io.api.service.RegistrationService.RegistrationRequest

//pass 0 to take all mock data (may be a large set)
class CoreTestData(val sampleSize: Int) {

  import CoreTestData._

  private val takeAll = if (sampleSize <= 0) true else false

  private lazy val fixtures = MockBilbyFixtures

  lazy val registrationRequests = fixtures.usersWithProfiles()
    .map(x => x._1 -> x._2.filter(profile => profile.loginInfo.providerID.equals(SupportedAuthProviders.CREDENTIALS.id)))
    .filter(!_._2.isEmpty).map(e => buildFakeRegistrationRequest(e._1, e._2.head)).take(if (!takeAll) sampleSize else fixtures.mockSize)

  lazy val authenticationRequests = registrationRequests.map(regReq => buildFakeAuthenticationRequest(regReq))

}

object CoreTestData {

  val testPassword = java.util.UUID.randomUUID.toString.substring(0, 10)

  def buildFakeRegistrationRequest(u: User, profile: UserProfile, method: String = "GET", route: String = "/") = {
    new RegistrationRequest {
      override val header = FakeRequest(method, route)
        .withHeaders(
          ("Date", java.time.LocalDateTime.now.toString()),
          ("Cookie", "empty"))
      override val data = RegistrationData(u, testPassword, profile.email.getOrElse("noemail@bilby.io"), profile)

      override val onSuccess = Results.Ok("registration onSuccess called")

      override val onFailure = (r: RegistrationRejection) => Results.Ok("denied because: " + r)
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