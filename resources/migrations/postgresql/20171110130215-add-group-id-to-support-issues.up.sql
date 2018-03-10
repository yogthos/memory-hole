alter table support_issues
  add column group_id text REFERENCES groups(group_id) DEFERRABLE;
update support_issues set group_id = (select group_id from groups where group_name = 'Default');
alter table support_issues ALTER COLUMN group_id SET NOT NULL;
