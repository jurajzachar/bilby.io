package com.blueskiron.bilby.io.db.activeslick

import io.strongtyped.active.slick.{ ActiveRecord, JdbcProfileProvider, Lens, EntityActions }
import slick.ast.BaseTypedType
import com.blueskiron.bilby.io.db.codegen.Tables

/**
 * Shows how to configure active-slick with a schema that was generated via slick-codegen. In this case,
 * A schema is generated on build as the object io.strongtyped.active.slick.docexamples.codegen.Tables.
 * See codegen_schema.sql for the schema that feeds into the codegen.
 */
object ActiveSlickRepos {
  
  abstract class UsersRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._
    
    type Entity = Tables.UsersRow
    type Id = Long
    type EntityTable = Tables.Users

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Users
    val idLens: Lens[Tables.UsersRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.UsersRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.UsersRow) extends ActiveRecord(UsersRepo)

  }
  
  abstract class AssetsRepo extends EntityActions with JdbcProfileProvider {

    override type JP = Tables.profile.type
    override val jdbcProfile = Tables.profile

    import jdbcProfile.api._

    type Entity = Tables.AssetsRow
    type Id = Long
    type EntityTable = Tables.Assets

    val baseTypedType = implicitly[BaseTypedType[Id]]
    val tableQuery = Tables.Assets
    val idLens: Lens[Tables.AssetsRow, Option[Long]] = {
      // For the getter, use 0L as a sentinel value because generated ID is usually non-optional
      Lens.lens { row: Tables.AssetsRow => if (row.id == 0L) None else Some(row.id) } { (row, maybeId) => maybeId map { id => row.copy(id = id) } getOrElse row }
    }

    override def $id(table: EntityTable): Rep[Long] = {
      table.id
    }

    implicit class EntryExtensions(val model: Tables.AssetsRow) extends ActiveRecord(AssetsRepo)

  }
  
   
  object UsersRepo extends UsersRepo
  object AssetsRepo extends AssetsRepo

}