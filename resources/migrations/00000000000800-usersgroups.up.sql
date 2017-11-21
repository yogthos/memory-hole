CREATE TABLE users_groups (
user_group_id serial NOT NULL,
user_id bigint NOT NULL references users (user_id) DEFERRABLE,
group_id text NOT NULL references groups (group_id) DEFERRABLE,
create_date timestamp not null default (now() at time zone 'utc'),
CONSTRAINT pk_users_groups PRIMARY KEY (user_group_id)
);
--;;
CREATE UNIQUE INDEX idx_user_id_group_id ON users_groups(user_id, group_id);

--;;
-- add the default admin user to the default group
INSERT INTO users_groups (user_id, group_id)
SELECT user_id, group_id
FROM users INNER JOIN groups ON (groups.group_name = 'Default' AND users.screenname = 'admin');
