--- !Downs
alter table "assetmetrics" drop constraint "id";
alter table "asset" drop constraint "author_id";
alter table "follower" drop constraint "id";
alter table "user" drop constraint "user_id";
alter table "account" drop constraint "account_id";
alter table "user" drop constraint "userprofile_id";
alter table "user" drop constraint "visitor_id";
drop table "countries";
drop table "reserved";
drop table "assetmetrics";
drop table "asset";
drop table "follower";
drop table "user";
drop table "userprofile";
drop table "visitor";
drop table "account";

