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

object TestUtils {
  val testPassword = java.util.UUID.randomUUID.toString.substring(0, 8)

  val fixtures = MockBilbyFixtures

  def regRequests(sampleSize: Int) = fixtures.usersWithProfiles()
    .map(x => x._1 -> x._2.filter(profile => profile.loginInfo.providerID.equals(SupportedAuthProviders.CREDENTIALS.id)))
    .filter(!_._2.isEmpty).map(e => TestUtils.buildFakeRegistrationRequest(e._1, e._2.head)).take(sampleSize)

  def authRequests(registrations: Iterable[RegistrationRequest]) = registrations.map(regReq => TestUtils.buildFakeAuthenticationRequest(regReq))

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