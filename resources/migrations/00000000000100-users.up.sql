CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
CREATE TABLE users
(user_id     SERIAL      NOT NULL PRIMARY KEY,
 screenname  VARCHAR(30) NOT NULL,
 first_name  VARCHAR(30),
 last_name   VARCHAR(50),
 email       VARCHAR(60),
 admin       BOOLEAN,
 last_login  TIMESTAMP,
 is_active   BOOLEAN,
 pass        VARCHAR(100),
 create_date TIMESTAMP   NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
 update_date TIMESTAMP   NOT NULL DEFAULT (now() AT TIME ZONE 'utc'));
--;;
-- create a system account for which the stock entries will be created
INSERT INTO users (screenname, first_name, last_name, email, admin, is_active, pass)
VALUES ('admin', 'superuser', 'maximus', 'admin@admin.com', TRUE, TRUE, '$2a$11$ni/3B3YdtYNyTNnCvlHmAe9k2XPZVHcb90dBbRw7UHVv7eFa4qMPS');