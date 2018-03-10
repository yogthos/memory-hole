-- :name groups :? :*
-- :doc Gets all the groups in the DB, in alphabetical order.
select * from groups order by group_name asc;

-- :name groups-for-user :? :*
-- :doc Gets groups user belongs to, or all groups if admin
select g.group_id, g.group_name
from groups g
left join users_groups ug
on g.group_id = ug.group_id
where ug.user_id = :user-id
or (select admin from users where user_id = :user-id);

-- :name groups-by-id :? :*
-- :doc Gets groups with ids in group-ids
select group_id, group_name
from groups
where group_id in (:v*:group-ids);

-- :name create-group<! :i! :1
-- :doc Inserts a new group.
insert into groups
/*~ (if (contains? params :group-id) */
(group_id, group_name)
values (:group-id, :group-name)
/*~*/
(group_name)
values (:group-name)
/*~) ~*/
;

-- :name add-user-to-groups! :! :n
-- :doc adds user :user-id to each group in :groups
insert into users_groups (user_id, group_id)
select :user-id, g.group_id
from groups g
where
g.group_id in (:v*:groups);

-- :name remove-user-from-groups! :! :n
-- :doc removes user :user-id from each group in :groups
delete from only users_groups ug
using groups g
where g.group_id = ug.group_id
and ug.user_id = :user-id
and g.group_id in (:v*:groups);
