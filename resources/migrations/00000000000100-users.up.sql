CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
CREATE TABLE users
(user_id     SERIAL      NOT NULL PRIMARY KEY,
 screenname  VARCHAR(30) NOT NULL,
 email       VARCHAR(60),
 admin       BOOLEAN,
 last_login  TIMESTAMP,
 is_active   BOOLEAN,
 create_date TIMESTAMP   NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
 update_date TIMESTAMP   NOT NULL DEFAULT (now() AT TIME ZONE 'utc'));
--;;
-- create a system account for which the stock entries will be created
INSERT INTO users (screenname, email, admin, is_active)
VALUES ('Admin', 'admin@admin.com', TRUE, TRUE);