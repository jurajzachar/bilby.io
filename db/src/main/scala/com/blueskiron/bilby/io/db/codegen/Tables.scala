package com.blueskiron.bilby.io.db.codegen

import com.blueskiron.bilby.io.db.codegen.Tables.profile.SchemaDescription
import slick.jdbc.{GetResult => GR}
import slick.lifted.ProvenShape.proveShapeOf
import slick.model.ForeignKeyAction
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = com.blueskiron.postgresql.slick.Driver
} with Tables
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: com.blueskiron.postgresql.slick.Driver
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Assets.schema, Oauth1Info.schema, Oauth2Info.schema, OpenidInfo.schema, PasswordInfo.schema, Requests.schema, Reserved.schema, SessionInfo.schema, UserProfiles.schema, Users.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Assets
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param title Database column title SqlType(varchar), Length(254,true)
   *  @param shortSummary Database column short_summary SqlType(text), Default(None)
   *  @param titleCover Database column title_cover SqlType(text), Default(None)
   *  @param published Database column published SqlType(timestamp), Default(None)
   *  @param authorId Database column author_id SqlType(int8)
   *  @param tags Database column tags SqlType(text), Default(None)
   *  @param source Database column source SqlType(text) */
  case class AssetsRow(id: Long, title: String, shortSummary: Option[String] = None, titleCover: Option[String] = None, published: Option[java.sql.Timestamp] = None, authorId: Long, tags: Option[String] = None, source: String)
  /** GetResult implicit for fetching AssetsRow objects using plain SQL queries */
  implicit def GetResultAssetsRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[java.sql.Timestamp]]): GR[AssetsRow] = GR{
    prs => import prs._
    AssetsRow.tupled((<<[Long], <<[String], <<?[String], <<?[String], <<?[java.sql.Timestamp], <<[Long], <<?[String], <<[String]))
  }
  /** Table description of table assets. Objects of this class serve as prototypes for rows in queries. */
  class Assets(_tableTag: Tag) extends Table[AssetsRow](_tableTag, "assets") {
    def * = (id, title, shortSummary, titleCover, published, authorId, tags, source) <> (AssetsRow.tupled, AssetsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(title), shortSummary, titleCover, published, Rep.Some(authorId), tags, Rep.Some(source)).shaped.<>({r=>import r._; _1.map(_=> AssetsRow.tupled((_1.get, _2.get, _3, _4, _5, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column title SqlType(varchar), Length(254,true) */
    val title: Rep[String] = column[String]("title", O.Length(254,varying=true))
    /** Database column short_summary SqlType(text), Default(None) */
    val shortSummary: Rep[Option[String]] = column[Option[String]]("short_summary", O.Default(None))
    /** Database column title_cover SqlType(text), Default(None) */
    val titleCover: Rep[Option[String]] = column[Option[String]]("title_cover", O.Default(None))
    /** Database column published SqlType(timestamp), Default(None) */
    val published: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("published", O.Default(None))
    /** Database column author_id SqlType(int8) */
    val authorId: Rep[Long] = column[Long]("author_id")
    /** Database column tags SqlType(text), Default(None) */
    val tags: Rep[Option[String]] = column[Option[String]]("tags", O.Default(None))
    /** Database column source SqlType(text) */
    val source: Rep[String] = column[String]("source")

    /** Foreign key referencing Users (database name author_id_fk) */
    lazy val usersFk = foreignKey("author_id_fk", authorId, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Assets */
  lazy val Assets = new TableQuery(tag => new Assets(tag))

  /** Entity class storing rows of table Oauth1Info
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param token Database column token SqlType(text)
   *  @param secret Database column secret SqlType(text)
   *  @param created Database column created SqlType(timestamp) */
  case class Oauth1InfoRow(provider: String, key: String, token: String, secret: String, created: java.sql.Timestamp)
  /** GetResult implicit for fetching Oauth1InfoRow objects using plain SQL queries */
  implicit def GetResultOauth1InfoRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[Oauth1InfoRow] = GR{
    prs => import prs._
    Oauth1InfoRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table oauth1_info. Objects of this class serve as prototypes for rows in queries. */
  class Oauth1Info(_tableTag: Tag) extends Table[Oauth1InfoRow](_tableTag, "oauth1_info") {
    def * = (provider, key, token, secret, created) <> (Oauth1InfoRow.tupled, Oauth1InfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(provider), Rep.Some(key), Rep.Some(token), Rep.Some(secret), Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> Oauth1InfoRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column token SqlType(text) */
    val token: Rep[String] = column[String]("token")
    /** Database column secret SqlType(text) */
    val secret: Rep[String] = column[String]("secret")
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Primary key of Oauth1Info (database name pk_oauth1_info) */
    val pk = primaryKey("pk_oauth1_info", (provider, key))
  }
  /** Collection-like TableQuery object for table Oauth1Info */
  lazy val Oauth1Info = new TableQuery(tag => new Oauth1Info(tag))

  /** Entity class storing rows of table Oauth2Info
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param accessToken Database column access_token SqlType(text)
   *  @param tokenType Database column token_type SqlType(varchar), Length(64,true), Default(None)
   *  @param expiresIn Database column expires_in SqlType(int4), Default(None)
   *  @param refreshToken Database column refresh_token SqlType(varchar), Length(64,true), Default(None)
   *  @param params Database column params SqlType(text), Default(None)
   *  @param created Database column created SqlType(timestamp), Default(None) */
  case class Oauth2InfoRow(provider: String, key: String, accessToken: String, tokenType: Option[String] = None, expiresIn: Option[Int] = None, refreshToken: Option[String] = None, params: Option[String] = None, created: Option[java.sql.Timestamp] = None)
  /** GetResult implicit for fetching Oauth2InfoRow objects using plain SQL queries */
  implicit def GetResultOauth2InfoRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Option[Int]], e3: GR[Option[java.sql.Timestamp]]): GR[Oauth2InfoRow] = GR{
    prs => import prs._
    Oauth2InfoRow.tupled((<<[String], <<[String], <<[String], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table oauth2_info. Objects of this class serve as prototypes for rows in queries. */
  class Oauth2Info(_tableTag: Tag) extends Table[Oauth2InfoRow](_tableTag, "oauth2_info") {
    def * = (provider, key, accessToken, tokenType, expiresIn, refreshToken, params, created) <> (Oauth2InfoRow.tupled, Oauth2InfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(provider), Rep.Some(key), Rep.Some(accessToken), tokenType, expiresIn, refreshToken, params, created).shaped.<>({r=>import r._; _1.map(_=> Oauth2InfoRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column access_token SqlType(text) */
    val accessToken: Rep[String] = column[String]("access_token")
    /** Database column token_type SqlType(varchar), Length(64,true), Default(None) */
    val tokenType: Rep[Option[String]] = column[Option[String]]("token_type", O.Length(64,varying=true), O.Default(None))
    /** Database column expires_in SqlType(int4), Default(None) */
    val expiresIn: Rep[Option[Int]] = column[Option[Int]]("expires_in", O.Default(None))
    /** Database column refresh_token SqlType(varchar), Length(64,true), Default(None) */
    val refreshToken: Rep[Option[String]] = column[Option[String]]("refresh_token", O.Length(64,varying=true), O.Default(None))
    /** Database column params SqlType(text), Default(None) */
    val params: Rep[Option[String]] = column[Option[String]]("params", O.Default(None))
    /** Database column created SqlType(timestamp), Default(None) */
    val created: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("created", O.Default(None))

    /** Primary key of Oauth2Info (database name pk_oauth2_info) */
    val pk = primaryKey("pk_oauth2_info", (provider, key))
  }
  /** Collection-like TableQuery object for table Oauth2Info */
  lazy val Oauth2Info = new TableQuery(tag => new Oauth2Info(tag))

  /** Entity class storing rows of table OpenidInfo
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param id Database column id SqlType(text)
   *  @param attributes Database column attributes SqlType(text)
   *  @param created Database column created SqlType(timestamp) */
  case class OpenidInfoRow(provider: String, key: String, id: String, attributes: String, created: java.sql.Timestamp)
  /** GetResult implicit for fetching OpenidInfoRow objects using plain SQL queries */
  implicit def GetResultOpenidInfoRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[OpenidInfoRow] = GR{
    prs => import prs._
    OpenidInfoRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table openid_info. Objects of this class serve as prototypes for rows in queries. */
  class OpenidInfo(_tableTag: Tag) extends Table[OpenidInfoRow](_tableTag, "openid_info") {
    def * = (provider, key, id, attributes, created) <> (OpenidInfoRow.tupled, OpenidInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(provider), Rep.Some(key), Rep.Some(id), Rep.Some(attributes), Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> OpenidInfoRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column id SqlType(text) */
    val id: Rep[String] = column[String]("id")
    /** Database column attributes SqlType(text) */
    val attributes: Rep[String] = column[String]("attributes")
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Primary key of OpenidInfo (database name pk_openid_info) */
    val pk = primaryKey("pk_openid_info", (provider, key))
  }
  /** Collection-like TableQuery object for table OpenidInfo */
  lazy val OpenidInfo = new TableQuery(tag => new OpenidInfo(tag))

  /** Entity class storing rows of table PasswordInfo
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param hasher Database column hasher SqlType(varchar), Length(64,true)
   *  @param password Database column password SqlType(varchar), Length(256,true)
   *  @param salt Database column salt SqlType(varchar), Length(256,true), Default(None)
   *  @param created Database column created SqlType(timestamp) */
  case class PasswordInfoRow(provider: String, key: String, hasher: String, password: String, salt: Option[String] = None, created: java.sql.Timestamp)
  /** GetResult implicit for fetching PasswordInfoRow objects using plain SQL queries */
  implicit def GetResultPasswordInfoRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[java.sql.Timestamp]): GR[PasswordInfoRow] = GR{
    prs => import prs._
    PasswordInfoRow.tupled((<<[String], <<[String], <<[String], <<[String], <<?[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table password_info. Objects of this class serve as prototypes for rows in queries. */
  class PasswordInfo(_tableTag: Tag) extends Table[PasswordInfoRow](_tableTag, "password_info") {
    def * = (provider, key, hasher, password, salt, created) <> (PasswordInfoRow.tupled, PasswordInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(provider), Rep.Some(key), Rep.Some(hasher), Rep.Some(password), salt, Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> PasswordInfoRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column hasher SqlType(varchar), Length(64,true) */
    val hasher: Rep[String] = column[String]("hasher", O.Length(64,varying=true))
    /** Database column password SqlType(varchar), Length(256,true) */
    val password: Rep[String] = column[String]("password", O.Length(256,varying=true))
    /** Database column salt SqlType(varchar), Length(256,true), Default(None) */
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Length(256,varying=true), O.Default(None))
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Primary key of PasswordInfo (database name pk_password_info) */
    val pk = primaryKey("pk_password_info", (provider, key))
  }
  /** Collection-like TableQuery object for table PasswordInfo */
  lazy val PasswordInfo = new TableQuery(tag => new PasswordInfo(tag))

  /** Entity class storing rows of table Requests
   *  @param id Database column id SqlType(uuid), PrimaryKey
   *  @param userId Database column user_id SqlType(int8)
   *  @param authProvider Database column auth_provider SqlType(varchar), Length(64,true)
   *  @param authKey Database column auth_key SqlType(text)
   *  @param remoteAddress Database column remote_address SqlType(varchar), Length(64,true)
   *  @param method Database column method SqlType(varchar), Length(10,true)
   *  @param host Database column host SqlType(text)
   *  @param secure Database column secure SqlType(bool)
   *  @param path Database column path SqlType(text)
   *  @param queryString Database column query_string SqlType(text), Default(None)
   *  @param lang Database column lang SqlType(text), Default(None)
   *  @param cookie Database column cookie SqlType(text), Default(None)
   *  @param referrer Database column referrer SqlType(text), Default(None)
   *  @param userAgent Database column user_agent SqlType(text), Default(None)
   *  @param started Database column started SqlType(timestamp)
   *  @param duration Database column duration SqlType(int4)
   *  @param status Database column status SqlType(int4) */
  case class RequestsRow(id: java.util.UUID, userId: Long, authProvider: String, authKey: String, remoteAddress: String, method: String, host: String, secure: Boolean, path: String, queryString: Option[String] = None, lang: Option[String] = None, cookie: Option[String] = None, referrer: Option[String] = None, userAgent: Option[String] = None, started: java.sql.Timestamp, duration: Int, status: Int)
  /** GetResult implicit for fetching RequestsRow objects using plain SQL queries */
  implicit def GetResultRequestsRow(implicit e0: GR[java.util.UUID], e1: GR[Long], e2: GR[String], e3: GR[Boolean], e4: GR[Option[String]], e5: GR[java.sql.Timestamp], e6: GR[Int]): GR[RequestsRow] = GR{
    prs => import prs._
    RequestsRow.tupled((<<[java.util.UUID], <<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Boolean], <<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[java.sql.Timestamp], <<[Int], <<[Int]))
  }
  /** Table description of table requests. Objects of this class serve as prototypes for rows in queries. */
  class Requests(_tableTag: Tag) extends Table[RequestsRow](_tableTag, "requests") {
    def * = (id, userId, authProvider, authKey, remoteAddress, method, host, secure, path, queryString, lang, cookie, referrer, userAgent, started, duration, status) <> (RequestsRow.tupled, RequestsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userId), Rep.Some(authProvider), Rep.Some(authKey), Rep.Some(remoteAddress), Rep.Some(method), Rep.Some(host), Rep.Some(secure), Rep.Some(path), queryString, lang, cookie, referrer, userAgent, Rep.Some(started), Rep.Some(duration), Rep.Some(status)).shaped.<>({r=>import r._; _1.map(_=> RequestsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10, _11, _12, _13, _14, _15.get, _16.get, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)
    /** Database column user_id SqlType(int8) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column auth_provider SqlType(varchar), Length(64,true) */
    val authProvider: Rep[String] = column[String]("auth_provider", O.Length(64,varying=true))
    /** Database column auth_key SqlType(text) */
    val authKey: Rep[String] = column[String]("auth_key")
    /** Database column remote_address SqlType(varchar), Length(64,true) */
    val remoteAddress: Rep[String] = column[String]("remote_address", O.Length(64,varying=true))
    /** Database column method SqlType(varchar), Length(10,true) */
    val method: Rep[String] = column[String]("method", O.Length(10,varying=true))
    /** Database column host SqlType(text) */
    val host: Rep[String] = column[String]("host")
    /** Database column secure SqlType(bool) */
    val secure: Rep[Boolean] = column[Boolean]("secure")
    /** Database column path SqlType(text) */
    val path: Rep[String] = column[String]("path")
    /** Database column query_string SqlType(text), Default(None) */
    val queryString: Rep[Option[String]] = column[Option[String]]("query_string", O.Default(None))
    /** Database column lang SqlType(text), Default(None) */
    val lang: Rep[Option[String]] = column[Option[String]]("lang", O.Default(None))
    /** Database column cookie SqlType(text), Default(None) */
    val cookie: Rep[Option[String]] = column[Option[String]]("cookie", O.Default(None))
    /** Database column referrer SqlType(text), Default(None) */
    val referrer: Rep[Option[String]] = column[Option[String]]("referrer", O.Default(None))
    /** Database column user_agent SqlType(text), Default(None) */
    val userAgent: Rep[Option[String]] = column[Option[String]]("user_agent", O.Default(None))
    /** Database column started SqlType(timestamp) */
    val started: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("started")
    /** Database column duration SqlType(int4) */
    val duration: Rep[Int] = column[Int]("duration")
    /** Database column status SqlType(int4) */
    val status: Rep[Int] = column[Int]("status")

    /** Foreign key referencing Users (database name requests_users_fk) */
    lazy val usersFk = foreignKey("requests_users_fk", userId, Users)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Requests */
  lazy val Requests = new TableQuery(tag => new Requests(tag))

  /** Entity class storing rows of table Reserved
   *  @param name Database column name SqlType(varchar), PrimaryKey, Length(254,true) */
  case class ReservedRow(name: String)
  /** GetResult implicit for fetching ReservedRow objects using plain SQL queries */
  implicit def GetResultReservedRow(implicit e0: GR[String]): GR[ReservedRow] = GR{
    prs => import prs._
    ReservedRow(<<[String])
  }
  /** Table description of table reserved. Objects of this class serve as prototypes for rows in queries. */
  class Reserved(_tableTag: Tag) extends Table[ReservedRow](_tableTag, "reserved") {
    def * = name <> (ReservedRow, ReservedRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(name).shaped.<>(r => r.map(_=> ReservedRow(r.get)), (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column name SqlType(varchar), PrimaryKey, Length(254,true) */
    val name: Rep[String] = column[String]("name", O.PrimaryKey, O.Length(254,varying=true))
  }
  /** Collection-like TableQuery object for table Reserved */
  lazy val Reserved = new TableQuery(tag => new Reserved(tag))

  /** Entity class storing rows of table SessionInfo
   *  @param id Database column id SqlType(text), PrimaryKey
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param lastUsed Database column last_used SqlType(timestamp)
   *  @param expiration Database column expiration SqlType(timestamp)
   *  @param fingerprint Database column fingerprint SqlType(text), Default(None)
   *  @param created Database column created SqlType(timestamp) */
  case class SessionInfoRow(id: String, provider: String, key: String, lastUsed: java.sql.Timestamp, expiration: java.sql.Timestamp, fingerprint: Option[String] = None, created: java.sql.Timestamp)
  /** GetResult implicit for fetching SessionInfoRow objects using plain SQL queries */
  implicit def GetResultSessionInfoRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp], e2: GR[Option[String]]): GR[SessionInfoRow] = GR{
    prs => import prs._
    SessionInfoRow.tupled((<<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<?[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table session_info. Objects of this class serve as prototypes for rows in queries. */
  class SessionInfo(_tableTag: Tag) extends Table[SessionInfoRow](_tableTag, "session_info") {
    def * = (id, provider, key, lastUsed, expiration, fingerprint, created) <> (SessionInfoRow.tupled, SessionInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(provider), Rep.Some(key), Rep.Some(lastUsed), Rep.Some(expiration), fingerprint, Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> SessionInfoRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(text), PrimaryKey */
    val id: Rep[String] = column[String]("id", O.PrimaryKey)
    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column last_used SqlType(timestamp) */
    val lastUsed: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_used")
    /** Database column expiration SqlType(timestamp) */
    val expiration: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expiration")
    /** Database column fingerprint SqlType(text), Default(None) */
    val fingerprint: Rep[Option[String]] = column[Option[String]]("fingerprint", O.Default(None))
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Index over (provider,key) (database name idx_session_info_provider_key) */
    val index1 = index("idx_session_info_provider_key", (provider, key))
  }
  /** Collection-like TableQuery object for table SessionInfo */
  lazy val SessionInfo = new TableQuery(tag => new SessionInfo(tag))

  /** Entity class storing rows of table UserProfiles
   *  @param provider Database column provider SqlType(varchar), Length(64,true)
   *  @param key Database column key SqlType(text)
   *  @param email Database column email SqlType(varchar), Length(256,true), Default(None)
   *  @param firstName Database column first_name SqlType(varchar), Length(512,true), Default(None)
   *  @param lastName Database column last_name SqlType(varchar), Length(512,true), Default(None)
   *  @param fullName Database column full_name SqlType(varchar), Length(512,true), Default(None)
   *  @param avatarUrl Database column avatar_url SqlType(text), Default(None)
   *  @param verified Database column verified SqlType(bool)
   *  @param created Database column created SqlType(timestamp) */
  case class UserProfilesRow(provider: String, key: String, email: Option[String] = None, firstName: Option[String] = None, lastName: Option[String] = None, fullName: Option[String] = None, avatarUrl: Option[String] = None, verified: Boolean, created: java.sql.Timestamp)
  /** GetResult implicit for fetching UserProfilesRow objects using plain SQL queries */
  implicit def GetResultUserProfilesRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Boolean], e3: GR[java.sql.Timestamp]): GR[UserProfilesRow] = GR{
    prs => import prs._
    UserProfilesRow.tupled((<<[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[Boolean], <<[java.sql.Timestamp]))
  }
  /** Table description of table user_profiles. Objects of this class serve as prototypes for rows in queries. */
  class UserProfiles(_tableTag: Tag) extends Table[UserProfilesRow](_tableTag, "user_profiles") {
    def * = (provider, key, email, firstName, lastName, fullName, avatarUrl, verified, created) <> (UserProfilesRow.tupled, UserProfilesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(provider), Rep.Some(key), email, firstName, lastName, fullName, avatarUrl, Rep.Some(verified), Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> UserProfilesRow.tupled((_1.get, _2.get, _3, _4, _5, _6, _7, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column provider SqlType(varchar), Length(64,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(64,varying=true))
    /** Database column key SqlType(text) */
    val key: Rep[String] = column[String]("key")
    /** Database column email SqlType(varchar), Length(256,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(256,varying=true), O.Default(None))
    /** Database column first_name SqlType(varchar), Length(512,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(512,varying=true), O.Default(None))
    /** Database column last_name SqlType(varchar), Length(512,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(512,varying=true), O.Default(None))
    /** Database column full_name SqlType(varchar), Length(512,true), Default(None) */
    val fullName: Rep[Option[String]] = column[Option[String]]("full_name", O.Length(512,varying=true), O.Default(None))
    /** Database column avatar_url SqlType(text), Default(None) */
    val avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url", O.Default(None))
    /** Database column verified SqlType(bool) */
    val verified: Rep[Boolean] = column[Boolean]("verified")
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Primary key of UserProfiles (database name pk_user_profile) */
    val pk = primaryKey("pk_user_profile", (provider, key))

    /** Index over (email) (database name user_profiles_email_idx) */
    val index1 = index("user_profiles_email_idx", email)
  }
  /** Collection-like TableQuery object for table UserProfiles */
  lazy val UserProfiles = new TableQuery(tag => new UserProfiles(tag))

  /** Entity class storing rows of table Users
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param username Database column username SqlType(varchar), Length(256,true)
   *  @param profiles Database column profiles SqlType(hstore), Length(2147483647,false)
   *  @param roles Database column roles SqlType(_varchar), Length(64,false)
   *  @param active Database column active SqlType(bool)
   *  @param created Database column created SqlType(timestamp) */
  case class UsersRow(id: Long, username: String, profiles: Map[String, String], roles: List[String], active: Boolean, created: java.sql.Timestamp)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Map[String, String]], e3: GR[List[String]], e4: GR[Boolean], e5: GR[java.sql.Timestamp]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[Long], <<[String], <<[Map[String, String]], <<[List[String]], <<[Boolean], <<[java.sql.Timestamp]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[UsersRow](_tableTag, "users") {
    def * = (id, username, profiles, roles, active, created) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(username), Rep.Some(profiles), Rep.Some(roles), Rep.Some(active), Rep.Some(created)).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username SqlType(varchar), Length(256,true) */
    val username: Rep[String] = column[String]("username", O.Length(256,varying=true))
    /** Database column profiles SqlType(hstore), Length(2147483647,false) */
    val profiles: Rep[Map[String, String]] = column[Map[String, String]]("profiles", O.Length(2147483647,varying=false))
    /** Database column roles SqlType(_varchar), Length(64,false) */
    val roles: Rep[List[String]] = column[List[String]]("roles", O.Length(64,varying=false))
    /** Database column active SqlType(bool) */
    val active: Rep[Boolean] = column[Boolean]("active")
    /** Database column created SqlType(timestamp) */
    val created: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created")

    /** Index over (profiles) (database name users_profiles_idx) */
    val index1 = index("users_profiles_idx", profiles)
    /** Index over (roles) (database name users_roles_idx) */
    val index2 = index("users_roles_idx", roles)
    /** Uniqueness Index over (username) (database name users_username_idx) */
    val index3 = index("users_username_idx", username, unique=true)
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
