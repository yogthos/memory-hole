-- :name groups :? :*
-- Gets all the groups in the DB, in alphabetical order.
select * from groups order by group_name asc;

-- :name create-group<! :<! :1
-- :doc Inserts a new group.
insert into groups (group_name)
  values (:group-name)
  returning group_id, group_name;