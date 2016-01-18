package com.blueskiron.bilby.io.db.service

import scala.concurrent.ExecutionContext

import org.slf4j.LoggerFactory

import com.blueskiron.bilby.io.db.PostgresDatabase
import com.blueskiron.bilby.io.db.dao.SessionInfoDao
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.daos.AuthenticatorDAO

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionInfoService[T <: PostgresDatabase] @Inject() (override protected val cake: T)
    extends AuthenticatorDAO[CookieAuthenticator] with ClosableDatabase[T]
    with SessionInfoDao {

  implicit val executionContext: ExecutionContext = cake.database.executor.executionContext
  
  lazy protected val dao = initSessionInfoDao(cake)

  override def find(id: String) = dao.findById(id)

  override def add(session: CookieAuthenticator) = dao.upsert(session)

  override def update(session: CookieAuthenticator) = dao.upsert(session)

  override def remove(id: String) = dao.removeById(id).map { _ => () } //unit

}