package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Routes

object JsRoutes extends Controller {

  /**
     * generate javascript routes
     */
    def all = Action { implicit request =>
      import routes.javascript._
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          PieceKeeper.overviewAction
        )
      ).as("text/javascript").withHeaders(VARY -> ACCEPT_ENCODING)
    }
    
}