--- TABLES
create table oauth1_info (
       provider character varying(64) not null,
       key text not null,
       token text not null,
       secret text not null,
       created timestamp without time zone not null,
       constraint pk_oauth1_info primary key (provider, key)
    ) with (oids = false);

create table oauth2_info (
       provider character varying(64) not null,
       key text not null,
       access_token text not null,
       token_type character varying(64),
       expires_in integer,
       refresh_token character varying(64),
       params text,
       created timestamp without time zone,
       constraint pk_oauth2_info primary key (provider, key)
    ) with (oids = false);

 create table openid_info (
       provider character varying(64) not null,
       key text not null,
       id text not null,
       attributes text not null,
       created timestamp without time zone not null,
       constraint pk_openid_info primary key (provider, key)
    ) with (oids = false);

create table password_info (
       provider character varying(64) not null,
       key text not null,
       hasher character varying(64) not null,
       password character varying(256) not null,
       salt character varying(256),
       created timestamp without time zone not null,
       constraint pk_password_info primary key (provider, key)
    ) with (oids = false);

create table users (
      id bigserial primary key,
      username character varying(256),
      profiles hstore not null,
      roles character varying(64)[] not null,
      active boolean not null,
      created timestamp not null
    ) with (oids=false);

create index users_profiles_idx on users using gin (profiles);
create unique index users_username_idx on users using btree (username collate pg_catalog."default");
create index users_roles_idx on users using gin (roles);

create table requests (
      id uuid primary key not null,
      user_id bigint not null,
      auth_provider character varying(64) not null,
      auth_key text not null,
      remote_address character varying(64) not null,

      method character varying(10) not null,
      host text not null,
      secure boolean not null,
      path text not null,
      query_string text,

      lang text,
      cookie text,
      referrer text,
      user_agent text,
      started timestamp not null,
      duration integer not null,
      status integer not null
    ) with (oids=false);

create index requests_account_idx on requests using btree (user_id);
alter table requests add constraint requests_users_fk foreign key (user_id) references users (id) on update no action on delete no action;

create table session_info (
      id text not null,
      provider character varying(64) not null,
      key text not null,
      last_used timestamp without time zone not null,
      expiration timestamp without time zone not null,
      fingerprint text,
      created timestamp without time zone not null,
      constraint pk_session_info primary key (id)
    ) with (oids = false);

create index idx_session_info_provider_key on session_info (provider, key);

create table user_profiles (
      provider character varying(64) not null,
      key text not null,
      email character varying(256),
      first_name character varying(512),
      last_name character varying(512),
      full_name character varying(512),
      avatar_url character varying(512),
      verified boolean not null,
      created timestamp not null
    ) with (oids=false);

create index user_profiles_email_idx on user_profiles using btree (email collate pg_catalog."default");
alter table user_profiles add constraint user_profiles_provider_key_idx unique (provider, key);

create table assets (
	id bigserial not null primary key,
	title varchar(254) not null,
	short_summary text,
	title_cover text,
	published timestamp,
	author_id bigint not null,
	tags text,
	source text not null) with (oids=false);

alter table assets add constraint author_id_fk foreign key (author_id) references users (id) on update no action on delete no action;

create table if not exists reserved (name varchar(254) not null primary key);

-- PERMS
alter table users owner TO play;
alter table assets owner TO play;
GRANT ALL on all tables in schema public to play;
GRANT ALL on all sequences in schema public to play;