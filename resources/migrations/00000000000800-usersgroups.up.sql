CREATE TABLE users_groups (
user_group_id serial NOT NULL,
user_id bigint NOT NULL references users (user_id) DEFERRABLE,
group_id bigint NOT NULL references groups (group_id) DEFERRABLE,
create_date timestamp not null default (now() at time zone 'utc'),
CONSTRAINT pk_users_groups PRIMARY KEY (user_group_id)
);
--;;
CREATE UNIQUE INDEX idx_user_id_group_id ON users_groups(user_id, group_id);


--;;
-- Add a view that puts groups array into belongs_to column
CREATE VIEW users_with_groups (user_id, screenname, pass, email, admin, last_login, is_active, create_date, update_date, belongs_to) AS
SELECT users.user_id, users.screenname, users.pass, users.email, users.admin, users.last_login, users.is_active, users.create_date, users.update_date, array_agg(groups.group_name)
FROM users, groups, users_groups
and users.user_id = users_groups.user_id
and groups.group_id = users_groups.group_id
group by users.user_id;

--;;
-- add the default admin user to the default group
INSERT INTO users_groups (user_id, group_id)
SELECT user_id, group_id
FROM users INNER JOIN groups ON (groups.group_name = 'Default' AND users.screenname = 'admin');
