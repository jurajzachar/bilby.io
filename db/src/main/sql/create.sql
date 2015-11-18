--- TABLES
create table if not exists "user" (
	"user_name" VARCHAR(254) NOT NULL,
	"account_id" BIGINT NOT NULL,
	"userprofile_id" BIGINT, 
	"visitor_id" BIGINT,
	"id" BIGSERIAL NOT NULL PRIMARY KEY);

create table if not exists "account" (
	"email" VARCHAR(254) NOT NULL,
	"password" VARCHAR(254) NOT NULL,
	"avatar_url" text NOT NULL,
	"auth_method" VARCHAR(254) NOT NULL,
	"oauth1" VARCHAR(254),
	"oauth2" VARCHAR(254),
	"passwordInfo" VARCHAR(254),
	"verified" BOOLEAN NOT NULL,
	"active" BOOLEAN NOT NULL,
	"id" BIGSERIAL NOT NULL PRIMARY KEY);

create table if not exists "userprofile" (
	"first_name" VARCHAR(254),
	"last_name" VARCHAR(254),
	"country" VARCHAR(254),
	"place_of_res" VARCHAR(254),
	"age" SMALLINT,
	"id" BIGSERIAL NOT NULL PRIMARY KEY);
			
create table if not exists "visitor" (
	"host" VARCHAR(254) NOT NULL,
	"timestamp" BIGINT NOT NULL,
	"id" BIGSERIAL NOT NULL PRIMARY KEY);
	
create table if not exists "asset" (
	"id" BIGSERIAL NOT NULL PRIMARY KEY,
	"title" VARCHAR(254) NOT NULL,
	"short_summary" text,
	"title_cover" text,
	"published" BIGINT,
	"author_id" BIGINT NOT NULL,
	"tags" text,
	"source" text NOT NULL);

create table if not exists "follower" ("id" BIGINT NOT NULL PRIMARY KEY,"fids" text NOT NULL);

--- slick driver does not support mapping for arrays so we are stuck with text for views
create table if not exists "assetmetrics" (
	"id" BIGINT NOT NULL PRIMARY KEY,
	"views" text,
	"likes" INTEGER NOT NULL,
	"dislikes" INTEGER NOT NULL);
	
create table if not exists "reserved" ("user_name" VARCHAR(254) NOT NULL PRIMARY KEY);
create table if not exists "countries" ("country_name" VARCHAR(254) NOT NULL PRIMARY KEY);
create unique index "unique_reserved" on "reserved" ("user_name");

--- INDICES
create unique index "unique_username" on "user" ("user_name");
create unique index "unique_email" on "account" ("email");

--- CONSTRAINTS
alter table "user" add constraint "userprofile_id" foreign key("userprofile_id") references "userprofile"("id") on update NO ACTION on delete NO ACTION;
alter table "user" add constraint "account_id" foreign key("account_id") references "account"("id") on update NO ACTION on delete NO ACTION;
alter table "user" add constraint "visitor_id" foreign key("visitor_id") references "visitor"("id") on update NO ACTION on delete NO ACTION;
alter table "follower" add constraint "id" foreign key("id") references "user"("id") on update NO ACTION on delete NO ACTION;
alter table "asset" add constraint "author_id" foreign key("author_id") references "user"("id") on update NO ACTION on delete NO ACTION;
alter table "assetmetrics" add constraint "id" foreign key("id") references "asset"("id") on update NO ACTION on delete NO ACTION;

-- PERMS
alter table "user" owner TO play;
alter table account owner TO play;
alter table userprofile owner TO play;
alter table visitor owner TO play;
alter table asset owner TO play;
GRANT ALL on all tables in schema public to play;
GRANT ALL on all sequences in schema public to play;