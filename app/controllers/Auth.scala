package controllers

import org.slf4j.LoggerFactory

import components.UserComponent
import models.User
import models.Visitor
import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Security
import play.filters.csrf.CSRF

object Auth extends Controller with UserComponent {

  val log = LoggerFactory.getLogger(this.getClass)

  val loginForm = Form(
    tuple(
      "id" -> text,
      "password" -> text) verifying ("Username or password does not match", result => result match {
        case (id, secret) => dal.authenticate(id, secret).isDefined
      }))

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(views.html.auth.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = CSFRHelper.withToken { token => implicit request =>
    loginForm.bindFromRequest.fold(
      hasErrors => BadRequest(views.html.auth.login(hasErrors)),
      valid => {
       dal.updateVisitor(valid._1, Visitor(Some(request.remoteAddress))) 
       wrapUp(valid._1) 
      })
  }

  def wrapUp(id: String) = {
    Redirect(routes.Application.index).withSession(
      Security.username -> id,
      "checked_in" -> System.currentTimeMillis().toString())

    //TO-DO: Do we need this ?
    //.withHeaders(
    //  CACHE_CONTROL -> "max-age=3600",
    //  ETAG -> "xx")
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  trait Secured {

    def username(request: RequestHeader) = request.session.get(Security.username)

    def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

    def withAuth(f: => String => Request[AnyContent] => Result) = {
      Security.Authenticated(username, onUnauthorized) { user =>
        Action(request => f(user)(request))
      }
    }

    /**
     * This method shows how you could wrap the withAuth method to also fetch your user
     * You will need to implement UserDAO.findOneByUsername
     */
    def withUser(f: User => Request[AnyContent] => Result) = withAuth { username =>
      implicit request => {
        dal.findUserById(username).map { user =>
          f(user)(request)
        }.getOrElse(onUnauthorized(request))
      }
    }
  }

  object CSFRHelper {
    def withToken(f: CSRF.Token => Request[AnyContent] => Result) = {
      Action { request =>
        import play.filters.csrf._
        CSRF.getToken(request) match {
          case None => BadRequest("CSRF token error!")
          case Some(token) => {
            log.info("Processing signed request token: {}", token)
            f(token)(request)
          }
        }
      }
    }
  }

}