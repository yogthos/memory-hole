CREATE TABLE groups (
  group_id serial NOT NULL PRIMARY KEY,
  group_name text NOT NULL UNIQUE,
  create_date timestamp not null default (now() at time zone 'utc')
);
--;;
-- create a default group
INSERT INTO groups (group_name)
VALUES ('Default');
