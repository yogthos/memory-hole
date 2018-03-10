CREATE TABLE groups (
  group_id varchar(255) default random_uuid()::varchar(255) NOT NULL PRIMARY KEY,
  group_name varchar(255) NOT NULL UNIQUE,
  create_date timestamp not null default (now())
);
--;;
-- create a default group
INSERT INTO groups (group_name)
VALUES ('Default');
