-- :name insert-user<! :<! :1
-- :doc add a user.
insert into users (screenname, admin, last_login, is_active)
     values (:screenname, :admin, (select now() at time zone 'utc'), :is-active)
returning user_id;

-- :name user-by-screenname :? :1
-- :doc get a user based on the id.
select user_id, screenname, admin, last_login, is_active
from users
where screenname = :screenname;

-- :name update-user<! :<! :1
-- :doc Updates the last login date and screenname for a user.
update users
set screenname =:screenname,
    last_login=(select now() at time zone 'utc')
where user_id=:user-id
returning user_id;