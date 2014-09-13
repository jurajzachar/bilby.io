package model

import org.squeryl.Schema
import org.squeryl.Table
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode
import org.squeryl.KeyedEntity

// The root object of the schema. Inheriting KeyedEntity[T] is not mandatory
// it just makes primary key methods available (delete and lookup) on tables.
class BilbyDBObject extends KeyedEntity[Long] {
  var id: Long = 0
}

object BilbyDB extends Schema {

  val visitorTable: Table[Visitor] =
    table[Visitor]("visitor")
  on(visitorTable) { v =>
    declare {
      v.id is (autoIncremented("visitor_id_seq"))
    }
  }

  val userTable: Table[User] =
    table[User]("user")
  on(userTable) { u =>
    declare {
      u.id is (autoIncremented("user_id_seq"))
    }
  }

  val userProfileTable: Table[UserProfile] =
    table[UserProfile]("userprofile")
  on(userProfileTable) { up =>
    declare {
      up.id is (autoIncremented("userprofile_id_seq"))
    }
  }

  val userProfileToUsers =
    oneToManyRelation(userProfileTable, userTable).
      via((up, u) => up.id === u.userprofile_id)
}
