package controllers

import play.api._
import play.api.mvc._
import scala.slick.codegen.SourceCodeGenerator
import play.api.data._
import play.api.data.Forms._
import views.html._
import org.slf4j.{Logger,LoggerFactory}

//case class GenerateForms(slickDriver: String, outputFolder: String, pkg: String, schema: Option[String])
//case class UisampleForms(textfield: String, selectfield: String, radiofield: String, datefield: String, filefield: String, passwordfield: String)

object Application extends Controller {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

//  def index = Action { request =>
//    request.session.get("email").map {
//      email => Ok(index(email))
//    }.getOrElse(Ok(index("")))
//  }
//
//  // -- Authentication
//  val loginForm = Form(
//    tuple(
//      "email" -> text,
//      "password" -> text) verifying ("Invalid email address or password", result => result match {
//        case (email, password) => User.authenticate(email, password).isDefined
//      }))
//
//  /**
//   * Login page.
//   */
//  def login = Action { implicit request =>
//    Ok(authenticate.login(loginForm))
//  }
//
//  /**
//   * Handle login form submission.
//   */
//  def authenticate = Action { implicit request =>
//    loginForm.bindFromRequest.fold(
//      formWithErrors => BadRequest(html.authenticate.login(formWithErrors)),
//      valid => {
//        val user: User = User.authenticate(valid._1, valid._2).get
//        val userProfile: UserProfile = UserProfile.findById(user.userprofile_id).get
//        user.profile = userProfile
//        logger.debug("Welcome back user " + user.username + "/" + user.email + "!")
//        Redirect(routes.Application.index).withSession("email" -> user.email)
//      })
//  }
//
//  /**
//   * Logout and clean the session.
//   */
//  def logout = Action {
//    Redirect(routes.Application.login).withNewSession.flashing(
//      "success" -> "You've been logged out")
//  }
//
//  /**
//   * Provide security features
//   */
//  trait Secured {
//
//    /**
//     * Retrieve the connected user email.
//     */
//    private def username(request: RequestHeader) = request.session.get("email")
//
//    /**
//     * Redirect to login if the user in not authorized.
//     */
//    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
//
//    // --
//
//    /**
//     * Action for authenticated users.
//     */
//    def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
//      Action(request => f(user)(request))
//    }

    /**
     * Check if the connected user is a member of this project.
     */
    //  def IsMemberOf(project: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    //    if(Project.isMember(project, user)) {
    //      f(user)(request)
    //    } else {
    //      Results.Forbidden
    //    }
    //  }
    //
    //  /**
    //   * Check if the connected user is a owner of this task.
    //   */
    //  def IsOwnerOf(task: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    //    if(Task.isOwner(task, user)) {
    //      f(user)(request)
    //    } else {
    //      Results.Forbidden
    //    }
    //  }

  /*  
  def show(page: String) = {

    import views.html._

    Action {
      implicit request =>
        {
          //update visitor count
          val hit: Visitor = new Visitor(0, request.host) 
          Visitor.insert(hit)
          
          page match {
            case "index"             => Ok(views.html.index())
            //case "about"             => Ok(about())
            //case "shut-up-manifesto" => Ok(views.html.shutUpManifesto())
            case _                   => NotFound(views.html.error404())
          }

        }
    }
  }
 */

}