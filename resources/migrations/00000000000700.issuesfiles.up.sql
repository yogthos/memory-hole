CREATE TABLE support_issues_files (
  support_issue_file_id serial NOT NULL,
  support_issue_id bigint NOT NULL references support_issues (support_issue_id) DEFERRABLE,
  file_id bigint NOT NULL references files (file_id) DEFERRABLE,
  create_date timestamp not null default (now() at time zone 'utc'),
  CONSTRAINT pk_support_issues_files PRIMARY KEY (support_issue_file_id)
) WITH ( OIDS=FALSE );
--;;
CREATE UNIQUE INDEX idx_support_issue_id_file_id ON support_issues_files(support_issue_id, file_id);
