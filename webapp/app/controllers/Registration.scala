package controllers

import javax.inject.Inject
import utils.BackendCore
import play.api.i18n.MessagesApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.RequestHeader
import scala.concurrent.Future
import views.html.{ reg => viewsReg }
import play.api.mvc.Action
import com.mohiva.play.silhouette.api.LoginInfo
import forms.UserForms.UserRegistrationForm
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationRequest
import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.api.model.SupportedAuthProviders
import org.joda.time.LocalDateTime
import com.blueskiron.bilby.io.api.model.Role
import com.blueskiron.bilby.io.api.model.UserProfile
import play.api.mvc.Request
import play.api.mvc.AnyContent
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationData
import com.blueskiron.bilby.io.api.RegistrationService.RegistrationRejection
import play.api.mvc.Result
import scala.concurrent.Promise

class Registration @Inject() (val core: BackendCore, val messagesApi: MessagesApi) extends SilhouetteController {

  import forms.UserForms.registrationForm
  override val env = core.authEnv

  def notFoundDefault(implicit request: RequestHeader) = Future.successful(NotFound(views.html.errors.notFound(request)))

  /**
   * Starts the sign up process. It shows a form that the user have to fill in and submit.
   */
  def startRegistration = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(_) => Redirect(routes.Application.index)
      case None    => Ok(viewsReg.register(registrationForm(core.userService)))
    })
  }

  def handleStartRegistration = Action.async { implicit request =>
    registrationForm(core.userService).bindFromRequest.fold(
      withErrors => Future.successful(BadRequest(viewsReg.register(withErrors))),
      data => {
        val registrationRequest = buildRegistrationRequest(data)
          core.regService ! registrationRequest
          registrationRequest.result.future
      })
  }

  private def buildRegistrationRequest(form: UserRegistrationForm)(implicit request: Request[AnyContent]): RegistrationRequest = { 
    val created = new LocalDateTime()
    val linfo = LoginInfo(SupportedAuthProviders.CREDENTIALS.id, form.email)
    val profiles = Seq(linfo)
    val roles = Set[Role](Role.User)
    val user: User = User(None, form.username, profiles, roles, false, created)
    val fullName = (form.firstName, form.lastName) match { case (Some(x), Some(y)) => Some(x + " " + y) case _ => None }
    val userProfile: UserProfile = UserProfile(linfo, Some(form.email), form.firstName, form.lastName, fullName, None, false, created)
    
    new RegistrationRequest {
      
      override val header = request
      
      override val data = RegistrationData(user, userProfile, userProfile.email.getOrElse("noemail@bilby.io"), form.password._1)

      override val onSuccess = Ok(viewsReg.afterRegister(true, None))

      override val onFailure = (r: RegistrationRejection) => Ok(viewsReg.afterRegister(false, None))
    }
  }
}