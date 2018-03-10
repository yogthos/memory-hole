CREATE TABLE support_issues_tags (
  support_issue_tag_id serial NOT NULL,
  support_issue_id bigint NOT NULL references support_issues (support_issue_id) DEFERRABLE,
  tag_id bigint NOT NULL references tags (tag_id) DEFERRABLE,
  create_date timestamp not null default (now() at time zone 'utc'),
  CONSTRAINT pk_support_issues_tags PRIMARY KEY (support_issue_tag_id)
);
--;;
CREATE UNIQUE INDEX idx_support_issue_id_tag_id ON support_issues_tags(support_issue_id, tag_id);
