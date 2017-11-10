alter table support_issues
add column group_name text not null REFERENCES groups(group_name) DEFERRABLE;
