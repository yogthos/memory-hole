-- :name insert-user<! :i! :1
-- :doc add a user
insert into users (screenname, admin, last_login, is_active, pass)
     values (:screenname,
     :admin,
     --~ (if (= :postgresql (:db-type params)) "(now() at time zone 'utc')," "now(),")
     :is-active,
     :pass);

-- :name user-by-screenname :? :1
-- :doc get a user based on the screenname
select u.user_id, screenname, array_agg(ug.group_id) as belongs_to, admin, last_login, is_active, pass
from users u
left join users_groups ug on u.user_id = ug.user_id
where screenname = :screenname
group by u.user_id;

-- :name users-by-screenname :? :*
-- :doc get a users with matching screennames
select u.user_id, screenname, array_agg(ug.group_id) as belongs_to, admin, last_login, is_active
from users u
left join users_groups ug on u.user_id = ug.user_id
where screenname like :screenname
group by u.user_id;

-- :name users-by-group :? :*
-- :doc get all the users in a group with a given group name
select u.user_id, screenname, array_agg(ug.group_id) as belongs_to, admin, last_login, is_active
from users u
left join users_groups ug on u.user_id = ug.user_id
inner join users_groups filter on u.user_id = filter.user_id
inner join groups g on filter.group_id = g.group_id
where g.group_name = :group-name
and is_active = true
group by u.user_id;

-- :name update-user-with-pass<! :! :1
-- :doc Updates all user fields
update users
set screenname = :screenname,
    pass = :pass,
    admin = :admin,
    is_active = :is-active,
    last_login=--~ (if (= :postgresql (:db-type params)) "(now() at time zone 'utc')" "now()")
where user_id=:user-id;

-- :name update-user<! :! :1
-- :doc Updates users fields except for pass
update users
set screenname = :screenname,
    admin = :admin,
    is_active = :is-active,
    last_login=--~ (if (= :postgresql (:db-type params)) "(now() at time zone 'utc')" "now()")
where user_id=:user-id;
