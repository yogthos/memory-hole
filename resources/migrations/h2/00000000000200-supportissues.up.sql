CREATE TABLE support_issues
(
  support_issue_id   SERIAL    NOT NULL,
  title              TEXT      NOT NULL,
  summary            TEXT      NOT NULL,
  detail             TEXT      NOT NULL,
  create_date        TIMESTAMP NOT NULL DEFAULT (now()),
  created_by         BIGINT    NOT NULL REFERENCES users (user_id),
  update_date        TIMESTAMP NOT NULL DEFAULT (now()),
  last_updated_by    BIGINT    NOT NULL REFERENCES users (user_id),
  delete_date        TIME      NULL,
  last_viewed_date   TIMESTAMP NOT NULL DEFAULT (now()),
  last_modified_date TIMESTAMP NOT NULL DEFAULT (now()),
  views              INTEGER   DEFAULT 1,
  CONSTRAINT pk_support_issues PRIMARY KEY (support_issue_id)
);
--;;
-- init H2 DB full text search
CREATE ALIAS IF NOT EXISTS FT_INIT FOR "org.h2.fulltext.FullText.init";
--;;
-- call init itself and create search index on desired columns
CALL FT_INIT();
--;;
CALL FT_CREATE_INDEX('PUBLIC', 'SUPPORT_ISSUES', 'TITLE, SUMMARY, DETAIL');
--;;
-- Reindexing is actually not needed (embedded) and not possible (commit during
-- trigger is disallowed by H2)
