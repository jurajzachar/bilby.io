package com.blueskiron.bilby.io.db.codegen

import com.blueskiron.bilby.io.api.model.User
import com.blueskiron.bilby.io.db.codegen.Tables.UsersRow
import scala.language.implicitConversions
import com.mohiva.play.silhouette.api.LoginInfo
import com.blueskiron.bilby.io.api.model.Role
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

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
  }

}