package controllers

import javax.inject.Inject
import play.api.mvc.Action
import play.api.i18n.{ MessagesApi, Messages }
import com.blueskiron.bilby.io.core.auth.AuthenticationEnvironment
import com.mohiva.play.silhouette.api.{ LoginInfo, SignUpEvent, LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.{ IdentityNotFoundException, InvalidPasswordException }
import scala.concurrent.Future
import views.html.{ auth => viewsAuth }
import utils.BackendCore
import play.api.mvc.Request
import play.api.mvc.AnyContent
import com.blueskiron.bilby.io.api.AuthenticationService.AuthRequest
import org.slf4j.LoggerFactory

class Auth @Inject() (override val core: BackendCore, val messagesApi: MessagesApi) extends BaseController(core) {

  import forms.UserForms.signInForm
  
  val log = LoggerFactory.getLogger(this.getClass)
  
  /**
   * Starts the sign in mechanism. It shows the login form.
   */
  def signIn = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(user) => Redirect(routes.Application.index)
      case None       => Ok(viewsAuth.signIn(signInForm))
    })
  }

  /**
   * Authenticates the user based on his email and password
   */
  def authenticate = Action.async { implicit request =>
    import scala.concurrent.ExecutionContext.Implicits._
    signInForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsAuth.signIn(formWithErrors))),
      formData => {
        val (identifier, password) = formData
//        val authRequest = buildAuthRequest(Credentials(identifier, password))
//        core.authService ! authRequest
//        authRequest.result.future
        
        env.credentials.authenticate(Credentials(identifier, password)).flatMap { loginInfo =>
          env.identityService.retrieve(loginInfo).flatMap {
            case Some(user) => for {
              authenticator <- env.authenticatorService.create(loginInfo)
              cookie <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(cookie, Redirect(routes.Application.index))
            } yield {
              env.eventBus.publish(LoginEvent(user, request, request2Messages))
              result
            }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }
        
        .recover {
          case e: ProviderException => {
            log.debug(s"provider error: $e")
            Redirect(routes.Auth.signIn).flashing("error" -> Messages("signin.auth.erorr"))
          }
        }
      })
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // SIGN OUT

  /**
   * Signs out the user
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, Redirect(routes.Application.index))
  }

  private def buildAuthRequest(creds: Credentials)(implicit request: Request[AnyContent]): AuthRequest = {

    new AuthRequest {
      override val credentials = creds
      override val header = request
      override val onSuccess = Redirect(routes.Application.index)
    }
  }
}