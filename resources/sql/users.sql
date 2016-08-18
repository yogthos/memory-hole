-- :name user-by-id :? :1
-- :doc get a user based on the id.
select id, display_name, admin, last_login, active
from users
  where id = :id;

-- :name sql-insert-user< :! :n
-- :doc add a user.
insert into users (id, display_name, pass, admin, last_login, active)
    values (:id, :display_name, :pass, :admin, :active);

-- :name update-last-login! :! :n
-- :doc Updates the last login date for a user.
update users
set last_login=(select now() at time zone 'utc')
where d=:id;