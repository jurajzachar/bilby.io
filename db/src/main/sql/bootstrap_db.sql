CREATE DATABASE bilby_io_test;
CREATE ROLE play WITH LOGIN PASSWORD 'play';
-- \connect bilby_io_test
GRANT ALL PRIVILEGES ON bilby_io_test TO play;
CREATE EXTENSION hstore;
