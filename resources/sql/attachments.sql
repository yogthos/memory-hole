--:name save-file! :! :n
-- saves a file to the database
insert into files
(type, name, data)
values (:type, :name, :data)

--:name load-file-data :? :1
-- retrieve file data by name
select type, data
from files
where name = :name

-- :name delete-file<! :<! :1
-- :doc removes file from the database
delete from files
where name = :name
returning name;

-- :name dissoc-file-from-issue! :! :n
-- :doc removes the file from the issue
delete from support_issues_files
where support_issue_id = :support-issue-id;

-- :name assoc-file-with-issue! :! :n
-- :doc assigns the file to a given support issue.
insert into support_issues_files (support_issue_id, file_id)
select :support-issue-id, f.file_id
  from files f where f.name = :name;

