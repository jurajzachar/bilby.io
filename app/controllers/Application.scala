package controllers

import play.api._
import play.api.mvc._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import model.BilbyDB
import model.{User,Visitor}
import views._
import common.AppEnv
import model.UserProfile

object Application extends Controller {

  val logger: Logger = LoggerFactory.getLogger("Bilby_Application")

  protected val env = new AppEnv(Play.unsafeApplication.configuration)

  /*
   * def index = Action { request =>
  		request.session.get("connected").map { user =>
    	Ok("Hello " + user)
  	}.getOrElse {
    	Unauthorized("Oops, you are not connected")
  		}
	}
   */
  def index = Action { request =>
    request.session.get("email").map {
      email => Ok(html.index(email))
    }.getOrElse(Ok(html.index("")))
  }

  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("Invalid email address or password", result => result match {
        case (email, password) => User.authenticate(email, password).isDefined
      }))

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.authenticate.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.authenticate.login(formWithErrors)),
      valid => {
        val user: User = User.authenticate(valid._1, valid._2).get
        val userProfile: UserProfile = UserProfile.findById(user.userprofile_id).get 
        user.profile = userProfile
        logger.debug("Welcome back user " + user.username + "/" + user.email + "!")
        Redirect(routes.Application.index).withSession("email" -> user.email)
      })
  }


  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  /**
   * Provide security features
   */
  trait Secured {

    /**
     * Retrieve the connected user email.
     */
    private def username(request: RequestHeader) = request.session.get("email")

    /**
     * Redirect to login if the user in not authorized.
     */
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

    // --

    /**
     * Action for authenticated users.
     */
    def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }

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

  }

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

  def index() = Action {

    Ok(views.html.index())

  }

  def picture(name: String) = Action {
    Ok.sendFile(new java.io.File(name)) // the name should contains the image extensions
  }

  def reviews() = Action {
    implicit request =>
      Ok(views.html.reviews.list(env.reviewService.reviewList(Some(env.dataReviewDirectory))))
  }

  def display(slug: String) = Action {

    implicit request =>
      env.reviewService.findReviewBySlug(slug, Some(env.dataReviewDirectory)) match {
        case Some(p) => Ok(views.html.reviews.review(p))
        case None    => NotFound(views.html.error404())
      }
  }
  
  def getALlVisitors: Int = Visitor.findAll.size
*/

}