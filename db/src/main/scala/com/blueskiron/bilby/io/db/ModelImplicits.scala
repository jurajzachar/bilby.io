package com.blueskiron.bilby.io.db

import scala.language.implicitConversions
import com.blueskiron.bilby.io.model._
import com.blueskiron.bilby.io.db.Tables.{UserRow, UserprofileRow, VisitorRow, PieceRow, PiecemetricsRow, FollowerRow}

/**
 * The aim of this class is to bridge the slick generated case classes
 * such as "UserRow" and model defined case classes ("User"). 
 * @author juri
 */
object ModelImplicits {
  
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
 
 implicit def pieceFromRows(pr: PieceRow) = {
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
 
}