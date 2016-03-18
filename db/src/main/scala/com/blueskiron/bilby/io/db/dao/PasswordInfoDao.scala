package com.blueskiron.bilby.io.db.dao

import com.blueskiron.bilby.io.db.PostgresDatabase
import scala.concurrent.ExecutionContext
import com.blueskiron.bilby.io.db.codegen.Tables
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.blueskiron.bilby.io.db.codegen.ModelImplicits
import org.joda.time.LocalDateTime

trait PasswordInfoDao {

  /**
   * Initialize this dao trait with a specific instance of ApplicationDatabase.
   * Pass implicit execution context within which all futures are scheduled.
   * (defaults to PostgresDatabase)
   * @param cake
   * @return
   */
  def initPasswordInfoDao[T <: PostgresDatabase](cake: T) = new PasswordInfoDao(cake)(cake.executionContext)

  protected class PasswordInfoDao[+T](val cake: PostgresDatabase)(implicit ex: ExecutionContext) {

    import cake.jdbcProfile.api._

    type PasswordInfoData = (PasswordInfo, LoginInfo, LocalDateTime)
    
    def findById(linfo: LoginInfo): Future[Option[PasswordInfo]] = {
      import ModelImplicits.ToModel
      for (pwinfoRow <- cake.runAction(compiledGetById(linfo.providerID, linfo.providerKey).result.headOption))
        yield pwinfoRow map ToModel.passwordInfoFromRow _
    }
    
    def upsert(data: PasswordInfoData): Future[PasswordInfo] = {
      import ModelImplicits.ToDataRow
      val passwordInfoRow =  ToDataRow.rowFromPasswordInfo(data._1, data._2, data._3)
      val q = getByIdQuery(data._2.providerID, data._2.providerKey)
      val insertIfExists = q.exists.result.flatMap { exists =>
        //update
        if(!exists) {
          Tables.PasswordInfo += passwordInfoRow 
        } else {
          q.update(passwordInfoRow)
        }
      }
      cake.commit(insertIfExists).flatMap { 
         //must affect one row only  
         case 1 => Future.successful(data._1) 
         case _ => Future.failed(new Exception("failed to save password info: " + data)) 
      }
    }
    
    /**
     * @param linfo
     */
    def removeById(linfo: LoginInfo): Future[Int] = {
      import ModelImplicits.ToModel
      for (deletedRows <- cake.runAction(compiledGetById(linfo.providerID, linfo.providerKey).delete))
        yield deletedRows //should affect one row only! 
    }

    val compiledGetById = Compiled(getByIdQuery _)

    private def getByIdQuery(providerId: Rep[String], providerKey: Rep[String]) = {
      for (pwinfo <- Tables.PasswordInfo.filter { x => x.provider === providerId && x.key === providerKey }) yield pwinfo
    }
    
  }
}