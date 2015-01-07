# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

-- create table "player" ("player_name" VARCHAR(254) NOT NULL,"id" SERIAL NOT NULL PRIMARY KEY);
-- create table "visitor" ("host" VARCHAR(254),"timestamp" BIGINT NOT NULL,"id" BIGSERIAL NOT NULL PRIMARY KEY);
-- create table "userprofile" ("country" VARCHAR(254),"place_of_res" VARCHAR(254),"age" SMALLINT,"id" BIGSERIAL NOT NULL PRIMARY KEY);
-- create table "user" ("first_name" VARCHAR(254),"last_name" VARCHAR(254),"user_name" VARCHAR(254) NOT NULL,"email" VARCHAR(254),"password" VARCHAR(254),"avatar_url" BYTEA NOT NULL,"auth_method" VARCHAR(254) NOT NULL,"oauth1" VARCHAR(254),"oauth2" VARCHAR(254),"passwordInfo" VARCHAR(254),"userprofile_id" BIGINT NOT NULL,"visitor_id" BIGINT NOT NULL,"id" BIGSERIAL NOT NULL PRIMARY KEY);
-- create unique index "unique_username" on "user" ("user_name");
-- create table "follower" ("id" BIGINT NOT NULL PRIMARY KEY,"fids" VARCHAR(254) NOT NULL);
-- alter table "user" add constraint "userprofile_id" foreign key("userprofile_id") references "userprofile"("id") on update NO ACTION on delete NO ACTION;
-- alter table "user" add constraint "visitor_id" foreign key("userprofile_id") references "visitor"("id") on update NO ACTION on delete NO ACTION;
-- alter table "follower" add constraint "id" foreign key("id") references "user"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "follower" drop constraint "id";
alter table "user" drop constraint "userprofile_id";
alter table "user" drop constraint "visitor_id";
drop table "follower";
drop table "user";
drop table "userprofile";
drop table "visitor";
drop table "player";

