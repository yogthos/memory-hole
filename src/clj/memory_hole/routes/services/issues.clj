(ns memory-hole.routes.services.issues
  (:require [memory-hole.db.core :as db]
            [schema.core :as s]
            [ring.util.http-response :refer :all]
            [memory-hole.routes.services.common :refer [handler]])
  (:import java.util.Date))

(def Tag
  {:tag-id                            s/Num
   :tag                               s/Str
   (s/optional-key :tag-count)        s/Num
   (s/optional-key :support-issue-id) s/Num
   (s/optional-key :create-date)      Date})

(def Issue
  {:support-issue-id             s/Num
   :title                        s/Str
   :group-id                     s/Str
   (s/optional-key :group-name)  s/Str
   :summary                      s/Str
   :detail                       s/Str
   (s/optional-key :create-date) Date
   :delete-date                  (s/maybe Date)
   :update-date                  (s/maybe Date)
   :last-updated-by              s/Num
   :last-viewed-date             Date
   :views                        s/Num
   :created-by                   s/Num
   (s/optional-key :files)       [s/Str]
   (s/optional-key :tags)        [s/Str]
   (s/optional-key :updated-by)  (s/maybe s/Num)
   :created-by-screenname        s/Str
   :updated-by-screenname        (s/maybe s/Str)})

(def IssueSummary
  (select-keys Issue
               [:support-issue-id
                :title
                :group-id
                (s/optional-key :group-name)
                :summary
                (s/optional-key :create-date)
                (s/optional-key :tags)
                :update-date
                :last-viewed-date
                :views]))

(def IssueResult
  {(s/optional-key :issue) Issue
   (s/optional-key :error) s/Str})

(def IssueSummaryResults
  {(s/optional-key :issues) [IssueSummary]
   (s/optional-key :error)  s/Str})

(def TagResult
  {(s/optional-key :tag)   Tag
   (s/optional-key :error) s/Str})

(def TagsResult
  {(s/optional-key :tags)  [Tag]
   (s/optional-key :error) s/Str})

(handler tags [m]
  (ok {:tags (db/ranked-tags m)}))

(handler all-issues [m]
  (ok {:issues (db/issues m)}))

(handler recent-issues [m]
  (ok {:issues (db/recently-viewed-issues m)}))

;; Don't need to check if user can access issue, only need to use built in group membership checking
(handler add-issue! [issue]
  (if-some [result (db/create-issue-with-tags! issue)]
    (ok result)
    (bad-request {:error (str "Issue not found for: " (select-keys issue [:user-id :support-issue-id]))})))

(handler update-issue! [issue]
  (if-some [result (db/run-query-if-user-can-access-issue
                    (select-keys issue [:user-id :support-issue-id])
                    #(db/update-issue-with-tags! issue))]
    (ok result)
    (bad-request {:error (str "Issue not found for: " (select-keys issue [:user-id :support-issue-id]))})))

(handler issue [{:keys [user-id support-issue-id] :as m}]
  (if-some [issue (db/run-query-if-user-can-access-issue
                   {:user-id user-id
                    :support-issue-id support-issue-id}
                   #(db/support-issue (dissoc m :user-id)))]
    (ok {:issue issue})
    (bad-request {:error (str "Issue not found for: " m)})))

(handler issues-by-views [m]
  (ok {:issues (db/issues-by-views m)}))

(handler issues-by-tag [m]
  (ok {:issues (db/issues-by-tag m)}))

(handler issues-by-group [m]
         (ok {:issues (db/issues-by-group m)}))

(handler search-issues [m]
         (ok {:issues (db/search-issues (-> m
                                            (update :query #(str "'" % "'"))
                                            (update :db-type (fn [x] db/*db-type*))))}))

(handler delete-issue! [{:keys [user-id support-issue-id] :as m}]
  (if-some [result (db/run-query-if-user-can-access-issue
                    {:user-id user-id
                     :support-issue-id support-issue-id}
                    #(db/dissoc-from-tags-and-delete-issue-and-files! (dissoc m :user-id)))]
    (ok result)
    (bad-request {:error (str "Issue not found for: " m)})))
