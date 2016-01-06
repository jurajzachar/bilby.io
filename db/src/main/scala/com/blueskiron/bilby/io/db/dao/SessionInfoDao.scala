package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.PostgresDatabase
import scala.concurrent.ExecutionContext
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import com.blueskiron.bilby.io.db.codegen.Tables
import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.api.LoginInfo

trait SessionInfoDao {

  /**
   * Initialize this dao trait with a specific instance of ApplicationDatabase.
   * Pass implicit execution context within which all futures are scheduled.
   * (defaults to PostgresDatabase)
   * @param cake
   * @return
   */
  def initSessionInfoDao[T <: PostgresDatabase](cake: T) = new SessionInfoDao(cake)(cake.executionContext)

  protected class SessionInfoDao[+T](val cake: PostgresDatabase)(implicit ex: ExecutionContext) {

    import cake.jdbcProfile.api._

        /**
     * @param linfo
     */
    def findById(id: String): Future[Option[CookieAuthenticator]] = {
      import ModelImplicits.ToModel
      for (sessionInfoRow <- cake.runAction(compiledGetById(id).result.headOption))
        yield sessionInfoRow map ToModel.cookieAuthenticatorFromRow _
    }
    
    /**
     * @param linfo
     * @return
     */
    def findByLoginInfo(linfo: LoginInfo): Future[Option[CookieAuthenticator]] = {
      import ModelImplicits.ToModel
      for (sessionInfoRow <- cake.runAction(compiledGetByLoginInfo(linfo.providerID, linfo.providerKey).result.headOption))
        yield sessionInfoRow map ToModel.cookieAuthenticatorFromRow _
    }

    /**
     * @param ca
     * @return
     */
    def upsert(ca: CookieAuthenticator): Future[CookieAuthenticator] = {
      import ModelImplicits.ToDataRow
      val sessionInfoRow = ToDataRow.rowFromCookieAuthenticator(ca)
      cake.runAction(Tables.SessionInfo.insertOrUpdate(sessionInfoRow)).flatMap {
        //must affect one row only  
        case 1 => Future.successful(ca)
        case _ => Future.failed(new Exception("failed to save cookie authenticator: " + ca))
      }
    }

    /**
     * @param linfo
     */
    def removeById(id: String): Future[Int] = {
      import ModelImplicits.ToModel
      for (deletedRows <- cake.runAction(compiledGetById(id).delete))
        yield deletedRows //should affect one row only! 
    }

    val compiledGetByLoginInfo = Compiled(getByLoginInfoQuery _)

    val compiledGetById = Compiled(getByIdQuery _)

    private def getByLoginInfoQuery(providerId: Rep[String], providerKey: Rep[String]) = {
      for (sessionInfo <- Tables.SessionInfo.filter { x => x.provider === providerId && x.key === providerKey }) yield sessionInfo
    }

    private def getByIdQuery(id: Rep[String]) = {
      for (sessionInfo <- Tables.SessionInfo.filter { _.id === id }) yield sessionInfo
    }
  }
}