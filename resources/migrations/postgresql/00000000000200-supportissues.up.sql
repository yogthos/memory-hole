CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
--;;
CREATE TABLE support_issues
(
  support_issue_id   SERIAL    NOT NULL,
  title              TEXT      NOT NULL,
  summary            TEXT      NOT NULL,
  detail             TEXT      NOT NULL,
  search_vector      TSVECTOR,
  create_date        TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  created_by         BIGINT    NOT NULL REFERENCES users (user_id),
  update_date        TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  last_updated_by    BIGINT    NOT NULL REFERENCES users (user_id),
  delete_date        TIME      NULL,
  last_viewed_date   TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  last_modified_date TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  views              INTEGER   DEFAULT 1,
  CONSTRAINT pk_support_issues PRIMARY KEY (support_issue_id)
);
--;;
-- index our search vector for faster searches
CREATE INDEX idx_search_vector ON support_issues USING GIST (search_vector);
--;;
-- create the update/insert search_vector function for support_issues.
-- This trigger creates a concatenated, weighted vector across
-- title, summary, and detail, where title is the heaviest weighted
-- and detail the least weighted.
-- for more info conuslt
-- http://www.postgresql.org/docs/9.1/static/textsearch-features.html#TEXTSEARCH-MANIPULATE-TSVECTOR
CREATE FUNCTION tsvector_update_trigger_support_issues()
  RETURNS TRIGGER AS $$
BEGIN
  new.search_vector :=
  setweight(to_tsvector('english', new.title), 'A') ||
  setweight(to_tsvector('english', new.summary), 'B') ||
  setweight(to_tsvector('english', new.detail), 'C');
  RETURN new;
END
$$ LANGUAGE plpgsql;
--;;
-- create the update/insert trigger for support_issues which will update
-- the search_vector column, allowing users to perform a weighted search
-- for issues.
CREATE TRIGGER search_vector_update BEFORE INSERT OR UPDATE
ON support_issues FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger_support_issues();
