-- :name add-issue<! :i! :1
-- :doc create a new issue
INSERT INTO support_issues (title, group_id, summary, detail, created_by, last_updated_by)
VALUES (:title, :group-id, :summary, :detail, :user-id, :user-id);

-- :name update-issue! :! :n
-- :doc Updates the issue using optimistic concurrency (last-in wins)
UPDATE support_issues
SET title            = :title,
    group_id         = :group-id,
    summary          = :summary,
    detail           = :detail,
    last_updated_by  = :user-id,
    update_date      = now(),
    last_viewed_date = now()
WHERE
  support_issue_id = :support-issue-id;

-- :name inc-issue-views! :! :1
-- :doc Updates the issue view count
UPDATE support_issues
SET views = views + 1
WHERE
  support_issue_id = :support-issue-id;

-- :name get-views-count :? :1
-- :doc Gets views count for given :support-issue-id
SELECT views FROM support_issues
WHERE
  support_issue_id = :support-issue-id;

-- :name support-issue* :? :1
-- :doc Gets the issue with the given support_issue_id
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.detail,
  si.create_date,
  si.update_date,
  si.delete_date,
  si.last_viewed_date,
  si.views,
  created.screenname as created_by_screenname,
  si.created_by,
  si.last_updated_by,
  updated.screenname as updated_by_screenname,
  array_agg(t.tag) as tags,
  array_agg(f.name) as files
FROM support_issues si
  INNER JOIN users created on si.created_by = created.user_id
  LEFT JOIN groups g on si.group_id = g.group_id
  LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
  LEFT JOIN tags t ON sit.tag_id = t.tag_id
  LEFT JOIN files f ON f.support_issue_id = si.support_issue_id
  LEFT OUTER JOIN users updated on si.last_updated_by = updated.user_id
WHERE
  si.support_issue_id = :support-issue-id
GROUP BY si.support_issue_id, created.user_id, updated.user_id, g.group_name;

-- :name issues :? :*
-- :doc Gets all the issues, in order of popularity.
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.create_date,
  si.update_date,
  si.last_viewed_date,
  array_agg(t.tag) as tags,
  si.views
FROM support_issues si
  LEFT JOIN groups g ON g.group_id = si.group_id
  LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
  LEFT JOIN tags t ON sit.tag_id = t.tag_id
WHERE (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
AND si.delete_date IS NULL
GROUP BY
  si.support_issue_id, g.group_name
ORDER BY last_viewed_date;

-- :name recently-viewed-issues :? :*
-- :doc Gets the top x number of issues, based on last views
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.create_date,
  si.update_date,
  si.last_viewed_date,
  array_agg(t.tag) as tags,
  si.views
FROM support_issues si
  LEFT JOIN groups g ON g.group_id = si.group_id
  LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
  LEFT JOIN tags t ON sit.tag_id = t.tag_id
WHERE
  si.delete_date IS NULL
  and (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
GROUP BY
  si.support_issue_id, g.group_name
ORDER BY si.last_viewed_date ASC
LIMIT :limit;

-- :name issues-by-views :? :*
-- :doc Gets all the issues, ordered by views
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.create_date,
  si.update_date,
  si.last_viewed_date,
  array_agg(t.tag) as tags,
  si.views
FROM support_issues si
  LEFT JOIN groups g ON g.group_id = si.group_id
  LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
  LEFT JOIN tags t ON sit.tag_id = t.tag_id
WHERE
  si.delete_date IS NULL
  and (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
GROUP BY si.support_issue_id, g.group_name
ORDER BY si.views DESC
OFFSET :offset
LIMIT :limit;

-- :name issues-by-tag :? :*
-- :doc Gets all the issues, in order of popularity, by a given tag.
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.create_date,
  si.update_date,
  si.last_viewed_date,
  array_agg(t.tag) as tags,
  si.views
FROM support_issues si
  LEFT JOIN groups g ON g.group_id = si.group_id
  LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
  LEFT JOIN tags t ON sit.tag_id = t.tag_id
  LEFT JOIN support_issues_tags sitfilter on si.support_issue_id = sitfilter.support_issue_id
  LEFT JOIN tags filter on sitfilter.tag_id = filter.tag_id
WHERE
  filter.tag = :tag
  AND delete_date IS NULL
  and (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
GROUP BY
  si.support_issue_id, g.group_name
ORDER BY last_viewed_date;

-- :name issues-by-group :? :*
-- :doc Gets all issues in order of popularity by group name.
SELECT
si.support_issue_id,
si.title,
si.group_id,
g.group_name,
si.summary,
si.create_date,
si.update_date,
si.last_viewed_date,
array_agg(t.tag) as tags,
si.views
FROM support_issues si
LEFT JOIN groups g ON g.group_id = si.group_id
LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
LEFT JOIN tags t ON sit.tag_id = t.tag_id
WHERE
g.group_name = :group-name
and (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
and delete_date IS NULL
GROUP BY
si.support_issue_id, g.group_name
ORDER BY last_viewed_date;

-- :name delete-issue! :! :n
-- :doc Deletes the support issue with the given support_issue_id
UPDATE support_issues
SET delete_date = now()
WHERE support_issue_id = :support-issue-id;

-- :name search-issues :? :*
-- :doc Search all support issues and returns, in order of relevance, any matching issue.
SELECT
  si.support_issue_id,
  si.title,
  si.group_id,
  g.group_name,
  si.summary,
  si.create_date,
  si.update_date,
  si.last_viewed_date,
  array_agg(t.tag) as tags,
  si.views
FROM support_issues si
  /*~ (if (= :postgresql (:db-type params)) */
  INNER JOIN (SELECT DISTINCT
                support_issue_id,
                ts_rank_cd(search_vector, to_tsquery(:query)) AS rank
              FROM support_issues, to_tsquery(:query) query
              WHERE query @@ search_vector
              ORDER BY rank DESC
              OFFSET :offset
              LIMIT :limit) x ON x.support_issue_id = si.support_issue_id
  /*~*/
  INNER JOIN (SELECT DISTINCT sit.support_issue_id
              FROM support_issues sit, FT_SEARCH_DATA(:query, :limit, :offset) FT
              WHERE FT.TABLE='SUPPORT_ISSUES' AND sit.support_issue_id = FT.KEYS[0]) x ON x.support_issue_id = si.support_issue_id
  /*~) ~*/
    LEFT JOIN groups g ON g.group_id = si.group_id
    LEFT JOIN support_issues_tags sit ON si.support_issue_id = sit.support_issue_id
    LEFT JOIN tags t ON sit.tag_id = t.tag_id
    WHERE si.delete_date IS NULL
    and (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
GROUP BY si.support_issue_id, g.group_name
ORDER BY last_viewed_date;


-- :name user-can-access-issue? :? :*
-- :doc Returns the issue if it can be edited by the user

SELECT si.support_issue_id
FROM support_issues si
WHERE (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
AND si.support_issue_id = :support-issue-id;
