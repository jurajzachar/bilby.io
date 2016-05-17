package controllers

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.Controller
import play.api.i18n.I18nSupport
import utils.BackendCore

/**
 * @author juri
 *
 */
abstract class BaseController(val core: BackendCore) extends SilhouetteController with I18nSupport {

  override val env = core.authEnv
  implicit val ec = core.executionContext
  
}