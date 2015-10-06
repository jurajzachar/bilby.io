package com.blueskiron.bilby.io.db

import io.strongtyped.active.slick.{ ActiveRecord, JdbcProfileProvider, Lens, EntityActions }
import slick.ast.BaseTypedType

/**
 * Shows how to configure active-slick with a schema that was generated via slick-codegen. In this case,
 * A schema is generated on build as the object io.strongtyped.active.slick.docexamples.codegen.Tables.
 * See codegen_schema.sql for the schema that feeds into the codegen.
 */
object ActiveSlickRepos {

  abstract class UserRepo extends EntityActions with JdbcProfileProvider {
    //
    // Implement JdbcProfileProvider with JDBCProfile from generated Tables.scala
    //
    override type JP = Tables.profile.type
    // Sucks that this is necessary. Did we have to define this type in JdbcProfileProvider? Why not just use JdbcProfile?
    override val jdbcProfile = Tables.profile

    //
    // Implement EntityActions
    //
    import jdbcProfile.api._

    type Entity = Tables.UserRow
    type Id = Long
    type EntityTable = Tables.User

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.User
    val idLens: Lens[Tables.UserRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.UserRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.UserRow) extends ActiveRecord(UserRepo)

  }

  abstract class UserprofileRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.UserprofileRow
    type Id = Long
    type EntityTable = Tables.Userprofile

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Userprofile
    val idLens: Lens[Tables.UserprofileRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.UserprofileRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.UserprofileRow) extends ActiveRecord(UserprofileRepo)

  }

  abstract class PieceRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.PieceRow
    type Id = Long
    type EntityTable = Tables.Piece

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Piece
    val idLens: Lens[Tables.PieceRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.PieceRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.PieceRow) extends ActiveRecord(PieceRepo)

  }
  
   abstract class PiecemetricsRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.PiecemetricsRow
    type Id = Long
    type EntityTable = Tables.Piecemetrics

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Piecemetrics
    val idLens: Lens[Tables.PiecemetricsRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.PiecemetricsRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.PiecemetricsRow) extends ActiveRecord(PiecemetricsRepo)

  }
   
  abstract class VisitorRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.VisitorRow
    type Id = Long
    type EntityTable = Tables.Visitor

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Visitor
    val idLens: Lens[Tables.VisitorRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.VisitorRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.VisitorRow) extends ActiveRecord(VisitorRepo)

  }

  abstract class FollowerRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.FollowerRow
    type Id = Long
    type EntityTable = Tables.Follower

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Follower
    val idLens: Lens[Tables.FollowerRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.FollowerRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.FollowerRow) extends ActiveRecord(FollowerRepo)

  }
  object UserRepo extends UserRepo
  object UserprofileRepo extends UserprofileRepo
  object PieceRepo extends PieceRepo
  object PiecemetricsRepo extends PiecemetricsRepo
  object VisitorRepo extends VisitorRepo
  object FollowerRepo extends FollowerRepo

}