-- :name create-tag<! :i! :1
-- :doc Insers a new tag.
insert into tags(tag)
    values (:tag);

-- :name tags :? :*
-- Gets all the tags in the DB, in alphabetical order.
select * from tags order by tag asc;

-- :name ranked-tags :? :*
-- :doc Gets all tags in the DB, ordered by tag name asc.
select count(si.*) as tag_count, t.tag_id, t.tag
from support_issues si
  left join groups g on g.group_id = si.group_id
  inner join support_issues_tags sit on si.support_issue_id = sit.support_issue_id
  inner join tags t on sit.tag_id = t.tag_id
  where si.delete_date is null
  AND (si.group_id IN (select group_id from users_groups where user_id = :user-id) or (select admin from users where user_id = :user-id))
group by t.tag, t.tag_id
order by t.tag asc;

-- :name tags-for-issue :? :*
-- :doc Gets all the tags for a given support issue.
select sit.support_issue_id, t.tag_id, t.tag
from tags t
  inner join support_issues_tags sit on t.tag_id = sit.tag_id
where
  sit.support_issue_id = :support-issue-id;

-- :name tags-with-names :? :*
-- :doc Gets all the tags with the given names.
select t.*
  from tags t
  where t.tag in (:v*:tags);

-- :name tags-for-issues :? :*
-- :doc Gets all the tags for a list of issues.
select sit.support_issue_id, t.tag_id, t.tag
  from tags t
  inner join support_issues_tags sit on t.tag_id = sit.tag_id
  where
    sit.support_issue_id in (:v*:support-issue-ids);

-- :name dissoc-tags-from-issue! :! :n
-- :doc Deletes all the tags associated to the provided issue.
delete from support_issues_tags
where support_issue_id = :support-issue-id;

-- :name assoc-tags-with-issue! :! :n
-- :doc assigns all the tags to a given support issue.
-- Assumes the list of tags given to the function are the actual tag
-- names, eg. 'whiteboard', 'pro', etc.
insert into support_issues_tags (support_issue_id, tag_id)
    select :support-issue-id, t.tag_id
      from tags t where t.tag in (:v*:tags);

