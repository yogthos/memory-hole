alter table support_issues add column group_id varchar(255);
alter table support_issues add constraint group_id_fk foreign key (group_id) references groups(group_id);
--;;
update support_issues set group_id = (select group_id from groups where group_name = 'Default');
alter table support_issues ALTER COLUMN group_id SET NOT NULL;
