-- name: sql-create-tag<!
-- Insers a new tag.
insert into supper.tags(tag)
    values(:tag);

-- name: sql-all-tags
-- Gets all the tags in the DB, in alphabetical order.
select *
from supper.tags
order by tag asc;

-- name: sql-get-ranked-tags
-- Gets all tags in the DB, ordered by tag name asc.
select count(si.*) as tag_count, t.tag
from supper.support_issues si
  inner join supper.support_issues_tags sit on si.support_issue_id = sit.support_issue_id
  inner join supper.tags t on sit.tag_id = t.tag_id
group by t.tag
order by t.tag asc;

-- name: sql-tags-for-support-issue
-- Gets all the tags for a given support issue.
select sit.support_issue_id, t.tag_id, t.tag
from supper.tags t
  inner join supper.support_issues_tags sit on t.tag_id = sit.tag_id
where
  sit.support_issue_id = :support_issue_id;

-- name: sql-tags-with-name
-- Gets all the tags with the given names.
select t.*
  from supper.tags t
  where t.tag in (:tags);

-- name: sql-tags-for-support-issues
-- Gets all the tags for a list of issues.
select sit.support_issue_id, t.tag_id, t.tag
  from supper.tags t
  inner join supper.support_issues_tags sit on t.tag_id = sit.tag_id
  where
    sit.support_issue_id in (:issue_ids);

-- name: sql-dissoc-tags-for-support-issue!
-- Deletes all the tags associated to the provided issue.
delete from supper.support_issues_tags
where support_issue_id = :issue_id;

-- name: sql-assoc-tag-to-support-issue!
-- assigns all the tags to a given support issue.
-- Assumes the list of tags given to the function are the actual tag
-- names, eg. 'whiteboard', 'pro', etc.
insert into supper.support_issues_tags (support_issue_id, tag_id)
    select :issue_id, t.tag_id
      from supper.tags t where t.tag in (:tags);

