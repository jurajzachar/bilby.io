package com.blueskiron.bilby.io.db

import scala.language.implicitConversions
import com.blueskiron.bilby.io.model._
import com.blueskiron.bilby.io.db.Tables.{UserRow, UserprofileRow, VisitorRow, PieceRow, PiecemetricsRow, FollowerRow}
import com.blueskiron.bilby.io.db.Tables.PiecemetricsRow
import com.blueskiron.bilby.io.db.Tables.UserprofileRow

/**
 * bridge for the slick-generated case classes
 * such as "UserRow" and model-defined case classes ("User"). 
 * @author juri
 */
object ModelImplicits {
  
 implicit def userprofileRowFromUserProfile(up: UserProfile) = {
   UserprofileRow(up.country, up.placeOfResidence, up.age, up.id.getOrElse(0L))
 } 
 
 implicit def userprofileFromUserprofileRow(upr: UserprofileRow) = {
   UserProfile(upr.country, upr.placeOfRes, upr.age, Some(upr.id))  
 }
 
 implicit def visitorRowFromVisitor(v: Visitor) = {
   VisitorRow(v.host, v.timestamp, v.id.getOrElse(0L))
 }
 
 implicit def visitorFromVisitorRow(vr: VisitorRow) = {
   Visitor(vr.host, vr.timestamp, Some(vr.id))  
 }
 
 implicit def followerRowFromFollower(f: Follower) = {
   FollowerRow(f.id.getOrElse(0L), f.fids.mkString(","))  
 }
 
 implicit def followerFromFollowerRow(fr: FollowerRow) = {
   Follower(Some(fr.id), fr.fids.split(",").map(_.toLong).toSet)  
 }
 
 implicit def userRowsFromUser(u: User): Tuple3[UserRow, Option[UserprofileRow], Option[VisitorRow]] = {
   val userProfileRow = u.userprofile map userprofileRowFromUserProfile
   val visitorRow =  u.visitor map visitorRowFromVisitor
   Tuple3(
   UserRow(
       u.firstName,
       u.lastName,
       u.userName,
       u.email,
       u.password,
       u.avatarUrl,
       u.authMethod,
       u.oAuth1Info,
       u.oAuth2Info,
       u.passwordInfo,
       userProfileRow.map(_.id),
       visitorRow.map(_.id),
       u.id.getOrElse(0L)
   ),
   userProfileRow,
   visitorRow
   )
 }
 
  implicit def userRowFromUserAndForeignKeys(u: User, userProfileId: Option[Long], visitorId: Option[Long]): UserRow = {
   UserRow(
       u.firstName,
       u.lastName,
       u.userName,
       u.email,
       u.password,
       u.avatarUrl,
       u.authMethod,
       u.oAuth1Info,
       u.oAuth2Info,
       u.passwordInfo,
       userProfileId,
       visitorId,
       u.id.getOrElse(0L)
   )
 }
  
 implicit def userFromRows(ur: UserRow, upr: Option[UserprofileRow], vr: Option[ VisitorRow]) = {
   val visitor = vr.map(v => Visitor(v.host, v.timestamp, Some(v.id)))
   val userProfile = upr.map(up => UserProfile(up.country, up.placeOfRes, up.age, Some(up.id)))
   User(
       ur.firstName,
       ur.lastName,
       ur.userName,
       ur.email,
       ur.password,
       ur.avatarUrl,
       ur.authMethod,
       ur.oauth1,
       ur.oauth2,
       ur.passwordinfo,
       userProfile,
       visitor,
       Some(ur.id))
 } 
 
 implicit def pieceFromRow(pr: PieceRow) = {
   Piece.flattenedPiece(
       Some(pr.id), 
       pr.title, 
       pr.shortSummary, 
       pr.titleCover,
       pr.published,
       pr.authorId,
       pr.tags.map(_.split(",").toSet).getOrElse(Set()),
       pr.source)
 }
 
 implicit def pieceMetricsRowFromPieceMetrics(pm: PieceMetrics) = {
   PiecemetricsRow(pm.id.getOrElse(0L), pm.views.map(_.mkString(",")), pm.likes, pm.dislikes)  
 }
 
 implicit def pieceMetricsFromRow(pmr: PiecemetricsRow) = {
   val viewIds = pmr.views.map(_.split(",").map(_.toLong).toSet)
   PieceMetrics(Some(pmr.id), viewIds, pmr.likes, pmr.dislikes)
 }
}