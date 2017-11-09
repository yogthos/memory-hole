-- :name insert-user<! :<! :1
-- :doc add a user
insert into users (screenname, admin, last_login, is_active, pass)
     values (:screenname, :admin, (select now() at time zone 'utc'), :is-active, :pass)
returning user_id;

-- :name user-by-screenname :? :1
-- :doc get a user based on the screenname
select user_id, screenname, belongs_to, admin, last_login, is_active, pass
from users_with_groups
where screenname = :screenname
and is_active = true;

-- :name users-by-screenname :? :*
-- :doc get a users with matching screennames
select user_id, screenname, belongs_to, admin, last_login, is_active
from users_with_groups
where screenname like :screenname;

-- :name users-by-group :? :*
-- :doc get all the users in a group
select user_id, belongs_to, screenname, admin, last_login, is_active
from users_with_groups
where ARRAY[:group-name] <@ belongs_to
and is_active = true;

-- :name update-user-with-pass<! :<! :1
-- :doc Updates all user fields
update users
set screenname = :screenname,
    pass = :pass,
    admin = :admin,
    is_active = :is-active,
    last_login=(select now() at time zone 'utc')
where user_id=:user-id
returning user_id, screenname, last_login, is_active, admin;

-- :name update-user<! :<! :1
-- :doc Updates users fields except for pass
update users
set screenname = :screenname,
    admin = :admin,
    is_active = :is-active,
    last_login=(select now() at time zone 'utc')
where user_id=:user-id
returning user_id, screenname, last_login, is_active, admin;
