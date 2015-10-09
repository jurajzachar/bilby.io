--- !Downs
alter table "piecemetrics" drop constraint "id";
alter table "piece" drop constraint "author_id";
alter table "follower" drop constraint "id";
alter table "user" drop constraint "userprofile_id";
alter table "user" drop constraint "visitor_id";
drop table "countries";
drop table "reserved";
drop table "piecemetrics";
drop table "piece";
drop table "follower";
drop table "user";
drop table "userprofile";
drop table "visitor";