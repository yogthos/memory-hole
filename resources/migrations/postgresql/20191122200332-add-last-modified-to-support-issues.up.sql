alter table support_issues
  add column last_modified_date TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc');

update support_issues set last_modified_date = (now() AT TIME ZONE 'utc');
alter table support_issues ALTER COLUMN last_modified_date SET NOT NULL;