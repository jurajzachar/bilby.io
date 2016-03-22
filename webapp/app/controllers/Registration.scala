package controllers

import javax.inject.Inject
import utils.silhouette.AuthenticationEnvironment
import utils.Mailer
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import views.html.{ registration => viewsRegistration }
import utils.MailService
import utils.silhouette.SilhouetteController
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import models.User
import utils.silhouette.Implicits._
import com.mohiva.play.silhouette.api.{ LoginInfo, SignUpEvent, LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.{ IdentityNotFoundException, InvalidPasswordException }
import models._

class Registration @Inject() (val env: AuthenticationEnvironment, val messagesApi: MessagesApi, val mailService: MailService) extends SilhouetteController {

  implicit val ms = mailService
  val passwordValidation = nonEmptyText(minLength = 8)
  def notFoundDefault(implicit request: RequestHeader) = Future.successful(NotFound(views.html.errors.notFound(request)))

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
  // SIGN UP
  /**
   * Starts the sign up mechanism. It shows a form that the user have to fill in and submit.
   */
  def startSignUp = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(_) => Redirect(routes.Application.index)
      case None => Ok(viewsRegistration.signUp(UserForms.registrationForm))
    })
  }

  /**
   * Handles the form filled by the user. The user and its password are saved and it sends him an email with a link to confirm his email address.
   */
  def handleStartSignUp = Action.async { implicit request =>
    signUpForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsRegistration.signUp(formWithErrors))),
      user => {
        val loginInfo: LoginInfo = user.email
        env.identityService.retrieve(loginInfo).flatMap {
          case Some(_) => Future.successful(BadRequest(viewsRegistration.signUp(signUpForm.withError("email", Messages("auth.user.notunique")))))
          case None => {
            val token = MailTokenUser(user.email, isSignUp = true)
            for {
              savedUser <- env.identityService.save(user)
              _ <- env.authInfoRepository.add(loginInfo, env.authInfo(user.password))
              _ <- env.tokenService.create(token)
            } yield {
              Mailer.welcome(savedUser, link = routes.Registration.signUp(token.id).absoluteURL())
              Ok(viewsRegistration.almostSignedUp(savedUser))
            }
          }
        }
      })
  }

  /**
   * Confirms the user's email address based on the token and authenticates him.
   */
  def signUp(tokenId: String) = Action.async { implicit request =>
    env.tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (token.isSignUp && !token.isExpired) => {
        env.identityService.retrieve(token.email).flatMap {
          case Some(user) => {
            env.authenticatorService.create(user.email).flatMap { authenticator =>
              if (!user.emailConfirmed) {
                env.identityService.save(user.copy(emailConfirmed = true)).map { newUser =>
                  env.publish(SignUpEvent(newUser, request, request2Messages))
                }
              }
              for {
                cookie <- env.authenticatorService.init(authenticator)
                result <- env.authenticatorService.embed(cookie, Ok(viewsRegistration.signedUp(user)))
              } yield {
                env.tokenService.consume(tokenId)
                env.publish(LoginEvent(user, request, request2Messages))
                result
              }
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
      case Some(token) => {
        env.tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => notFoundDefault
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // FORGOT PASSWORD

  val emailForm = Form(single("email" -> email))

  /**
   * Starts the reset password mechanism if the user has forgot his password. It shows a form to insert his email address.
   */
  def forgotPassword = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(_) => Redirect(routes.Application.index)
      case None => Ok(viewsRegistration.forgotPassword(emailForm))
    })
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def handleForgotPassword = Action.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsRegistration.forgotPassword(formWithErrors))),
      email => env.identityService.retrieve(email).flatMap {
        case Some(_) => {
          val token = MailTokenUser(email, isSignUp = false)
          env.tokenService.create(token).map { _ =>
            Mailer.forgotPassword(email, link = routes.Registration.resetPassword(token.id).absoluteURL())
            Ok(viewsRegistration.forgotPasswordSent(email))
          }
        }
        case None => Future.successful(BadRequest(viewsRegistration.forgotPassword(emailForm.withError("email", Messages("auth.user.notexists")))))
      })
  }

  val resetPasswordForm = Form(tuple(
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText) 
    verifying (Messages("auth.passwords.notequal"), passwords => passwords._2 == passwords._1))

  /**
   * Confirms the user's link based on the token and shows him a form to reset the password
   */
  def resetPassword(tokenId: String) = Action.async { implicit request =>
    env.tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        Future.successful(Ok(viewsRegistration.resetPassword(tokenId, resetPasswordForm)))
      }
      case Some(token) => {
        env.tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => notFoundDefault
    }
  }

  /**
   * Saves the new password and authenticates the user
   */
  def handleResetPassword(tokenId: String) = Action.async { implicit request =>
    resetPasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsRegistration.resetPassword(tokenId, formWithErrors))),
      passwords => {
        env.tokenService.retrieve(tokenId).flatMap {
          case Some(token) if (!token.isSignUp && !token.isExpired) => {
            val loginInfo: LoginInfo = token.email
            env.identityService.retrieve(loginInfo).flatMap {
              case Some(user) => {
                for {
                  _ <- env.authInfoRepository.update(loginInfo, env.authInfo(passwords._1))
                  authenticator <- env.authenticatorService.create(user.email)
                  result <- env.authenticatorService.renew(authenticator, Ok(viewsRegistration.resetPasswordComplete(user)))
                } yield {
                  env.tokenService.consume(tokenId)
                  env.publish(LoginEvent(user, request, request2Messages))
                  result
                }
              }
              case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          }
          case Some(token) => {
            env.tokenService.consume(tokenId)
            notFoundDefault
          }
          case None => notFoundDefault
        }
      })
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CHANGE PASSWORD

  val changePasswordForm = Form(tuple(
    "current" -> nonEmptyText,
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText) verifying (Messages("auth.passwords.notequal"), passwords => passwords._3 == passwords._2))

  /**
   * Starts the change password mechanism. It shows a form to insert his current password and the new one.
   */
  def changePassword = SecuredAction.async { implicit request =>
    Future.successful(Ok(viewsRegistration.changePassword(changePasswordForm)))
  }

  /**
   * Saves the new password and renew the cookie
   */
  def handleChangePassword = SecuredAction.async { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(viewsRegistration.changePassword(formWithErrors))),
      passwords => {
        env.credentialsProvider.authenticate(Credentials(request.identity.email, passwords._1)).flatMap { loginInfo =>
          for {
            _ <- env.authInfoRepository.update(loginInfo, env.authInfo(passwords._2))
            authenticator <- env.authenticatorService.create(loginInfo)
            result <- env.authenticatorService.renew(authenticator, Redirect(routes.Application.myAccount).flashing("success" -> Messages("auth.password.changed")))
          } yield result
        }.recover {
          case e: ProviderException => BadRequest(viewsRegistration.changePassword(changePasswordForm.withError("current", Messages("auth.currentpwd.incorrect"))))
        }
      })
  }
}