# --- !Ups
create table if not exists "visitor" ("host" VARCHAR(254) NOT NULL,"timestamp" BIGINT NOT NULL,"id" BIGSERIAL NOT NULL PRIMARY KEY);
create table if not exists "userprofile" ("country" VARCHAR(254),"place_of_res" VARCHAR(254),"age" SMALLINT,"id" BIGSERIAL NOT NULL PRIMARY KEY);
create table if not exists "user" ("first_name" VARCHAR(254),"last_name" VARCHAR(254),"user_name" VARCHAR(254) NOT NULL,"email" VARCHAR(254) NOT NULL,"password" VARCHAR(254) NOT NULL,"avatar_url" text,"auth_method" VARCHAR(254) NOT NULL,"oauth1" VARCHAR(254),"oauth2" VARCHAR(254),"passwordInfo" VARCHAR(254),"userprofile_id" BIGINT NOT NULL,"visitor_id" BIGINT NOT NULL,"id" BIGSERIAL NOT NULL PRIMARY KEY);
create unique index "unique_id" on "user" ("id");
create unique index "unique_username" on "user" ("user_name");
create table if not exists "follower" ("id" BIGINT NOT NULL PRIMARY KEY,"fids" text);
create table if not exists "piece" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"title" VARCHAR(254) NOT NULL,"short_summary" text,"title_cover" text,"published" BIGINT,"author_id" BIGINT NOT NULL,"tags" text,"source" text NOT NULL);
create table if not exists "piecemetrics" ("id" BIGINT NOT NULL PRIMARY KEY,"views" text,"likes" INTEGER,"dislikes" INTEGER);
create table if not exists "reserved" ("user_name" VARCHAR(254) NOT NULL PRIMARY KEY);
create unique index "unique_reserved" on "reserved" ("user_name");
alter table "user" add constraint "userprofile_id" foreign key("userprofile_id") references "userprofile"("id") on update NO ACTION on delete NO ACTION;
alter table "user" add constraint "visitor_id" foreign key("visitor_id") references "visitor"("id") on update NO ACTION on delete NO ACTION;
alter table "follower" add constraint "id" foreign key("id") references "user"("id") on update NO ACTION on delete NO ACTION;
alter table "piece" add constraint "author_id" foreign key("author_id") references "user"("id") on update NO ACTION on delete NO ACTION;
alter table "piecemetrics" add constraint "id" foreign key("id") references "piece"("id") on update NO ACTION on delete NO ACTION;