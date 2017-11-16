CREATE VIEW users_visible_issues
(user_id, support_issue_id)
AS
SELECT u.user_id, i.support_issue_id
FROM users_with_groups u
LEFT JOIN support_issues i
ON ARRAY[i.group_name] <@ u.belongs_to OR u.admin;
