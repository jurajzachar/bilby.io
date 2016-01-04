package com.blueskiron.bilby.io.db.codegen

import scala.language.implicitConversions
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import com.blueskiron.bilby.io.api.model.Role
import com.blueskiron.bilby.io.api.model.{ User, UserProfile }
import com.blueskiron.bilby.io.db.codegen.Tables.UserProfilesRow
import com.blueskiron.bilby.io.db.codegen.Tables.UsersRow
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.bilby.io.db.codegen.Tables.PasswordInfoRow
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.blueskiron.bilby.io.db.codegen.Tables.SessionInfoRow

object ModelImplicits {

  object ToDataRow {

    /**
     * Convert model User type to database row type
     * @param u
     * @return
     */
    implicit def rowFromUser(u: User): UsersRow = {
      UsersRow(
        u.id.getOrElse(0L),
        u.username,
        u.profiles.map(l => s"${l.providerID}" -> s"${l.providerKey}").toMap,
        u.roles.map(_.name).toList,
        u.active,
        new java.sql.Timestamp(u.created.toDateTime().getMillis))
    }

    /**
     * @param up
     * @return
     */
    implicit def rowFromUserProfile(up: UserProfile): UserProfilesRow = {
      UserProfilesRow(
        up.loginInfo.providerID,
        up.loginInfo.providerKey,
        up.email,
        up.firstname,
        up.lastname,
        up.fullname,
        up.avatarUrl,
        up.verified,
        new java.sql.Timestamp(up.created.toDateTime().getMillis))
    }

    /**
     * ! not an implicit -> composite tuple3
     * @param pwi
     * @return
     */
    def rowFromPasswordInfo(pwi: PasswordInfo, linfo: LoginInfo, created: LocalDateTime): PasswordInfoRow = {
      PasswordInfoRow(
        linfo.providerID, linfo.providerKey, pwi.hasher, pwi.password, pwi.salt, new java.sql.Timestamp(created.toDateTime().getMillis))
    }

    /**
     * @param ca
     * @return
     */
    implicit def rowFromCookieAuthenticator(ca: CookieAuthenticator): SessionInfoRow = {
      SessionInfoRow(
        ca.id,
        ca.loginInfo.providerID,
        ca.loginInfo.providerKey,
        new java.sql.Timestamp(ca.lastUsedDateTime.toDateTime().getMillis),
        new java.sql.Timestamp(ca.expirationDateTime.toDateTime().getMillis),
        ca.fingerprint,
        new java.sql.Timestamp(new LocalDateTime().toDateTime().getMillis))
    }
  }

  object ToModel {

    /**
     * Convert database row type to model User type
     * @param ur
     * @return
     */
    implicit def userFromRow(ur: UsersRow): User = {
      User(
        Some(ur.id),
        ur.username,
        ur.profiles.map(entry => LoginInfo(entry._1, entry._2)).toList,
        ur.roles.map(Role(_)).toSet,
        ur.active,
        LocalDateTime.fromDateFields(ur.created))
    }

    /**
     * @param up
     * @return
     */
    implicit def userProfileFromRow(up: UserProfilesRow): UserProfile = {
      UserProfile(
        LoginInfo(up.provider, up.key),
        up.email,
        up.firstName,
        up.lastName,
        up.fullName,
        up.avatarUrl,
        up.verified,
        LocalDateTime.fromDateFields(up.created))
    }

    /**
     * @param pwir
     * @return
     */
    implicit def passwordInfoFromRow(pwir: PasswordInfoRow): PasswordInfo = {
      PasswordInfo(pwir.hasher, pwir.password, pwir.salt)
    }

    /**
     * @param sir
     * @return
     */
    implicit def cookieAuthenticatorFromRow(sir: SessionInfoRow): CookieAuthenticator = {
      CookieAuthenticator(
        sir.id,
        LoginInfo(sir.provider, sir.key),
        LocalDateTime.fromDateFields(sir.lastUsed).toDateTime(),
        LocalDateTime.fromDateFields(sir.expiration).toDateTime(),
        idleTimeout = None,
        cookieMaxAge = None,
        sir.fingerprint)
    }
  }

}