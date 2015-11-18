package com.blueskiron.bilby.io.db.ar

import scala.language.implicitConversions
import com.blueskiron.bilby.io.db.Tables.{ FollowerRow, AssetRow, AssetmetricsRow, UserRow, UserprofileRow, VisitorRow, AccountRow }
import com.blueskiron.bilby.io.api.model._

/**
 * bridge for the slick-generated case classes
 * TODO: get rid off this boilerplate and use something like Shapeless
 * @author juri
 */
object ModelImplicits {

  object ToDataRow {

    implicit def rowFromUserProfile(up: UserProfile) = {
      UserprofileRow(up.firstName, up.lastName, up.country, up.placeOfRes, up.age, up.id.getOrElse(0L))
    }

    implicit def rowFromVisitor(v: Visitor) = {
      VisitorRow(v.host, v.timestamp, v.id.getOrElse(0L))
    }

    implicit def rowFromAccount(a: Account): AccountRow = {
      AccountRow(
        a.email,
        a.password,
        a.avatarUrl,
        a.authMethod,
        a.oAuth1Info,
        a.oAuth2Info,
        a.passwordInfo,
        a.verified,
        a.active,
        a.id.getOrElse(0L))
    }

    implicit def rowFromUser(u: User): UserRow = {
      val accountRow = rowFromAccount(u.account)
      val userProfileRow = u.userprofile map rowFromUserProfile
      val visitorRow = u.visitor map rowFromVisitor
      UserRow(
        u.userName,
        accountRow.id,
        userProfileRow.map(_.id),
        visitorRow.map(_.id),
        u.id.getOrElse(0L))
    }

    implicit def rowFromUserNameAndForeignKeys(uname: String, accountId: Long, userProfileId: Option[Long], visitorId: Option[Long], id: Option[Long]): UserRow = {
      UserRow(
        uname,
        accountId,
        userProfileId,
        visitorId,
        id.getOrElse(0L))
    }

    implicit def rowFromAsset(p: Asset) = {
      AssetRow(
        p.id.getOrElse(0L),
        p.header.title,
        p.header.shortSummary,
        p.header.titleCoverUrl,
        p.published,
        p.authorId,
        Some(p.header.tags.mkString(",")),
        p.header.source)
    }

    implicit def rowFromAssetMetrics(pm: AssetMetrics) = {
      AssetmetricsRow(pm.id.getOrElse(0L), pm.views.map(_.mkString(",")), pm.likes, pm.dislikes)
    }

  }
  
  object ToModel {
    
    implicit def userprofileFromRow(upr: UserprofileRow) = {
      UserProfile(upr.firstName, upr.lastName, upr.country, upr.placeOfRes, upr.age, Some(upr.id))
    }

    implicit def visitorFromRow(vr: VisitorRow) = {
      Visitor(vr.host, vr.timestamp, Some(vr.id))
    }

    // implicit def followerRowFromFollower(f: Follower) = {
    //   FollowerRow(f.leads.mkString(","), f.id.getOrElse(0L))  
    // }
    // 
    // implicit def followerFromFollowerRow(fr: FollowerRow) = {
    //   Follower(fr.leads.split(",").map(_.toLong).toSet, Some(fr.id))  
    // }

    implicit def accountFromRow(accR: AccountRow) = {
      Account(
        accR.email,
        accR.password,
        accR.avatarUrl,
        accR.authMethod,
        accR.oauth1,
        accR.oauth2,
        accR.passwordinfo,
        accR.verified,
        accR.active,
        Some(accR.id))
    }

    implicit def userFromRows(ur: UserRow, accR: AccountRow, upr: Option[UserprofileRow], vr: Option[VisitorRow]) = {
      User(
        ur.userName,
        accR,
        upr map userprofileFromRow,
        vr map visitorFromRow,
        Some(ur.id))
    }

    implicit def assetFromRow(pr: AssetRow) = {
      Asset.flattenedAsset(
        Some(pr.id),
        pr.title,
        pr.shortSummary,
        pr.titleCover,
        pr.published,
        pr.authorId,
        pr.tags.map(_.split(",").toSet).getOrElse(Set()),
        pr.source)
    }

    implicit def assetMetricsFromRow(pmr: AssetmetricsRow) = {
      val viewIds = pmr.views.map(_.split(",").map(_.toLong).toSet)
      AssetMetrics(Some(pmr.id), viewIds, pmr.likes, pmr.dislikes)
    }
    
  }
}