package db

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import org.slf4j.LoggerFactory

import PostgresSpec.cake.Followers
import PostgresSpec.cake.UserProfilesExtensions
import PostgresSpec.cake.Users
import PostgresSpec.cake.UsersExtensions
import PostgresSpec.cake.VisitorExtenstions
import PostgresSpec.cake.createSchema
import PostgresSpec.cake.dropSchema
import PostgresSpec.cake.jdbcDriver.simple.queryToAppliedQueryInvoker
import PostgresSpec.cake.jdbcDriver.simple.queryToInsertInvoker
import PostgresSpec.cake.jdbcDriver.simple.repToQueryExecutor
import models.User

class SchemaSpec extends FlatSpec 
  with PostgresSpec 
  with MustMatchers 
  with BeforeAndAfter {

  import PostgresSpec.cake._
  
  val log = LoggerFactory.getLogger(this.getClass)
  
  before {
    database withSession {
      implicit session =>
        log.info("Creating database schema...")
        try {
          createSchema
        } catch {
          case t: Throwable => println(t.getMessage) // TODO: handle error
        }
    }
  }

  after {
    database withSession {
      implicit session =>
        try {
          log.info("Dropping database schema...")
          dropSchema
        } catch {
          case t: Throwable => t.printStackTrace() // TODO: handle error
        }
    }
  }

  "save" should "persist records in their respective tables" in new MockCase {
    import jdbcDriver.simple._
    //Vistors
    database withTransaction { implicit session =>
      visitors.foreach(_.save)
    }
    //UserProfiles
    database withTransaction { implicit session =>
      userProfiles.foreach(_.save)
    }
    //Users
    database withTransaction { implicit session =>
      //randomly assign user profiles to users.
      connectedUsers.foreach(_.save) //persist users
      Users.count must be(1000)
    }
    //Followers (non-active record)
    database withTransaction { implicit session =>
      followers.foreach(Followers += _)
    }
    
//    database withTransaction { implicit session =>
//      Followers.length.run must be(1000)
//      Followers.list.foreach {
//        x =>
//          {
//            val user = Users.findById(x.id)
//            val leads: Set[User] = for (id <- x.fids) yield Users.findById(id)
//            println(s"user ${user.username} follows: ${leads.size} other users.")
//          }
//      }
//    }
  }

}