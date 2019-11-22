alter table support_issues
  add column last_modified_date TIMESTAMP DEFAULT (now());

update support_issues set last_modified_date = (now());
alter table support_issues ALTER COLUMN last_modified_date SET NOT NULL;