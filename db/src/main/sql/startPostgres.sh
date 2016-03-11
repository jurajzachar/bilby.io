#!/bin/bash

#/usr/local/opt/postgresql/bin/postgres -D /usr/local/var/postgres -r /usr/local/var/postgres/server.log

pg_ctl start -l /usr/local/var/postgres/server.log -D /usr/local/var/postgres
