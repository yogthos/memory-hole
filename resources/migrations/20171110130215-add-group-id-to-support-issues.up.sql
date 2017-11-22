alter table support_issues
add column group_id text not null REFERENCES groups(group_id) DEFERRABLE;
