-- :name groups :? :*
-- Gets all the groups in the DB, in alphabetical order.
select * from groups order by group_name asc;

-- :name create-group<! :<! :1
-- :doc Inserts a new group.
insert into groups (group_name)
  values (:group-name)
  returning group_id, group_name;

-- :name add-user-to-groups! :! :n
-- :doc adds user :user-id to each group in :groups
insert into users_groups (user_id, group_id)
select :user-id, g.group_id
from groups g where g.group_name in (:v*:groups);

-- :name remove-user-from-groups! :! :n
-- :doc removes user :user-id from each group in :groups
delete from only users_groups ug
using groups g
where g.group_id = ug.group_id
and ug.user_id = :user-id
and g.group_name in (:v*:groups);
