--:name save-file! :! :n
-- saves a file to the database
insert into files
(support_issue_id, type, name, data)
values (:support-issue-id, :type, :name, :data)

--:name load-file-data :? :1
-- retrieve file data by name
select type, data
from files
where support_issue_id = :support-issue-id
and   name = :name

-- :name delete-issue-files! :! :n
delete from files
where support_issue_id = :support-issue-id

-- :name delete-file<! :! :1
-- :doc removes file from the database
delete from files
where support_issue_id = :support-issue-id
and name = :name;
