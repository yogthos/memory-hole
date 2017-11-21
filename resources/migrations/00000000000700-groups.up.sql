CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE groups (
  group_id text NOT NULL PRIMARY KEY default gen_random_uuid()::text,
  group_name text NOT NULL UNIQUE,
  create_date timestamp not null default (now() at time zone 'utc')
);
--;;
-- create a default group
INSERT INTO groups (group_name)
VALUES ('Default');
