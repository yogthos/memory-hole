CREATE TABLE files
(support_issue_id bigint NOT NULL references support_issues (support_issue_id) DEFERRABLE,
 name  VARCHAR(150) NOT NULL,
 type  VARCHAR(50) NOT NULL,
 create_date timestamp not null default (now() at time zone 'utc'),
 data  BYTEA,
 PRIMARY KEY(support_issue_id, name));


