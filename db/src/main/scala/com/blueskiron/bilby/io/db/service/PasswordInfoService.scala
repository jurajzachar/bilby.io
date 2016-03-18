package com.blueskiron.bilby.io.db.service

import scala.concurrent.ExecutionContext

import org.joda.time.LocalDateTime
import org.slf4j.LoggerFactory

import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.dao.PasswordInfoDao
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordInfoService[T <: PostgresDatabase] @Inject() (protected val cake: T) extends DelegableAuthInfoDAO[PasswordInfo]
    with PasswordInfoDao {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit val executionContext: ExecutionContext = cake.database.executor.executionContext

  lazy val dao = initPasswordInfoDao(cake)

  override def find(loginInfo: LoginInfo) = dao.findById(loginInfo)

  //TODO: SILHOUETTE: what is the difference between save and add?
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = dao.upsert((authInfo, loginInfo, new LocalDateTime()))

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = dao.upsert((authInfo, loginInfo, new LocalDateTime()))

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo) = dao.upsert((authInfo, loginInfo, new LocalDateTime()))

  override def remove(loginInfo: LoginInfo) = dao.removeById(loginInfo).map { x => logger.info("removed {} loginInfo={}", x, loginInfo) }
  
}