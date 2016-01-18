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
class PasswordInfoService [T <: PostgresDatabase] @Inject() (override val cake: T) extends DelegableAuthInfoDAO[PasswordInfo] with ClosableDatabase[T]
with PasswordInfoDao {
  
  private val logger = LoggerFactory.getLogger(getClass)
  
  implicit val executionContext: ExecutionContext = cake.database.executor.executionContext
  
  lazy val dao = initPasswordInfoDao(cake)
  
  override def find(loginInfo: LoginInfo) = dao.findById(loginInfo)
  
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    logger.debug("saving new loginInfo={}", loginInfo)
    dao.upsert((loginInfo, authInfo, new LocalDateTime()))
  }
  
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = dao.upsert((loginInfo, authInfo, new LocalDateTime()))
  
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo) = dao.upsert((loginInfo, authInfo, new LocalDateTime()))
  
  override def remove(loginInfo: LoginInfo) = dao.removeById(loginInfo).map { x => logger.info("removed {} loginInfo={}", x, loginInfo) }
  
}