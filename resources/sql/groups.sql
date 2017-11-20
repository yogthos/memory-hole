-- :name groups :? :*
-- Gets all the groups in the DB, in alphabetical order.
select * from groups order by group_name asc;

-- :name groups-for-user :? :*
-- Gets groups user belongs to, or all groups if admin
select distinct g.group_id, g.group_name from groups g
left join users_groups ug
on g.group_id = ug.group_id
where ug.user_id = :user-id
or (select admin from users where user_id = :user-id);

-- :name create-group<! :<! :1
-- :doc Inserts a new group.
insert into groups
/*~ (if (contains? params :distinguished-name) */
(group_name, distinguished_name)
values (:group-name, :distinguished-name)
/*~*/
(group_name)
values (:group-name)
/*~) ~*/

returning group_id, group_name;

-- :name add-user-to-groups! :! :n
-- :doc adds user :user-id to each group in :groups
insert into users_groups (user_id, group_id)
select :user-id, g.group_id
from groups g
where
g.group_name in (:v*:groups)
or
g.distinguished_name in (:v*:groups);

-- :name remove-user-from-groups! :! :n
-- :doc removes user :user-id from each group in :groups
delete from only users_groups ug
using groups g
where g.group_id = ug.group_id
and ug.user_id = :user-id
and (g.group_name in (:v*:groups)
    or g.distinguished_name in (:v*:groups));

