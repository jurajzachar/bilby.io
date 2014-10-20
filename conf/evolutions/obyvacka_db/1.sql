-- !Ups
CREATE TABLE "userprofile"
(
  id serial NOT NULL,
  country character varying,
  city character varying,
  age smallint,
  CONSTRAINT userprofile_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE userprofile
  OWNER TO play;

CREATE TABLE "user"
(
  id serial NOT NULL,
  username character varying,
  password character varying,
  email character varying,
  userprofile_id bigint,
  CONSTRAINT user_id PRIMARY KEY (id),
  CONSTRAINT userprofile_id FOREIGN KEY (userprofile_id)
      REFERENCES userprofile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT unique_username UNIQUE (username)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "user"
  OWNER TO play;
  
CREATE TABLE "visitor"
(
  host character varying,
  "timestamp" bigint,
  id serial NOT NULL,
  CONSTRAINT visitor_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE visitor
  OWNER TO play;
  
-- !Downs
drop table if exists "userprofile" cascade;
drop table if exists "user" cascade;
drop table if exists "visitor" cascade;
  