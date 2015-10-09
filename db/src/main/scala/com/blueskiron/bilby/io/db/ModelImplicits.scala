package com.blueskiron.bilby.io.db

import scala.language.implicitConversions
import com.blueskiron.bilby.io.model._
import com.blueskiron.bilby.io.db.Tables.{UserRow, UserprofileRow, VisitorRow, PieceRow, PiecemetricsRow, FollowerRow}

/**
 * bridge for the slick-generated case classes
 * such as "UserRow" and model-defined case classes ("User"). 
 * @author juri
 */
object ModelImplicits {
  
 implicit def userprofileRowFromUserProfile(up: UserProfile) = {
   UserprofileRow(up.country, up.placeOfResidence, up.age, up.id.getOrElse(0L))
 } 
 implicit def visitorRowFromVisitor(v: Visitor) = {
   VisitorRow(v.host, v.timestamp, v.id.getOrElse(0L))
 }
 
 implicit def userRowsFromUser(u: User): Tuple3[UserRow, Option[UserprofileRow], Option[VisitorRow]] = {
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
       u.userprofile.map(_.id.getOrElse(0L)).getOrElse(0L),
       u.visitor.map(_.id.getOrElse(0L)).getOrElse(0L),
       u.id.getOrElse(0L)
   ),
   u.userprofile map userprofileRowFromUserProfile,
   u.visitor map visitorRowFromVisitor
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
 
 implicit def pieceMetricsFromRow(pmr: PiecemetricsRow) = {
   val viewIds = pmr.views.map(_.split(",").map(_.toLong).toSet).getOrElse(Set())
   PieceMetrics(pmr.id, viewIds, pmr.likes, pmr.dislikes)
 }
}