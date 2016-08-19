(ns yuggoth.routes.services.issues
  (:require [yuggoth.db.core :as db]
            [schema.core :as s]
            [ring.util.http-response :refer :all]
            [yuggoth.routes.services.common :refer [handler]])
  (:import java.util.Date))

(def Tag
  {:tag-id                            s/Num
   :tag                               s/Str
   (s/optional-key :support-issue-id) s/Num
   (s/optional-key :create-date)      Date})

(def Issue
  {:support-issue-id             s/Num
   :title                        s/Str
   :summary                      s/Str
   :detail                       s/Str
   (s/optional-key :create-date) Date
   :delete-date                  (s/maybe Date)
   :update-date                  (s/maybe Date)
   :last-updated-by              s/Num
   :last-viewed-date             Date
   :views                        s/Num
   :created-by                   s/Num
   (s/optional-key :tags)        [Tag]
   (s/optional-key :updated-by)  (s/maybe s/Num)
   :created-by-screenname        s/Str
   :updated-by-screenname        (s/maybe s/Str)})

(def IssueSummary
  (select-keys Issue
               [:support-issue-id
                :title
                :summary
                (s/optional-key :create-date)
                :update-date
                :last-viewed-date
                :views]))

(def IssueResult
  {(s/optional-key :issue) Issue
   (s/optional-key :error) s/Str})

(def IssueSummaryResults
  {(s/optional-key :issues) [IssueSummary]
   (s/optional-key :error)  s/Str})

(def TagsResult
  {(s/optional-key :tags)  [Tag]
   (s/optional-key :error) s/Str})

(handler recent-issues [limit]
  (ok {:issues (db/recently-viewed-issues {:limit limit})}))

(handler add-issue! [issue]
  (ok (:support-issue-id (db/add-issue<! issue))))

(handler update-issue! [issue]
  (ok (db/update-issue! issue)))

(handler issue [m]
  (if-let [issue (db/support-issue m)]
    (ok {:issue issue})
    (bad-request {:error (str "Issue not found for: " m)})))

(handler issues-by-views [m]
  (ok {:issues (db/issues-by-views m)}))

(handler issues-by-tag [m]
  (ok {:issues (db/issues-by-tag m)}))

(handler search-issues [m]
  (ok {:issues (db/search-issues m)}))

(handler delete-issue! [m]
  (ok (db/delete-issue! m)))

#_(s/validate IssueResult (:body (issue {:support-issue-id 1})))

#_(s/validate Issue (db/support-issue {:support-issue-id 1}))

#_(s/validate [IssueSummary] (db/recently-viewed-issues {:limit 2}))

#_(s/validate [IssueSummary] (db/issues-by-tag {:tag "supper"}))

#_(s/validate
    [IssueSummary]
    (db/search-issues {:query "supper" :limit 10 :offset 0}))

#_(s/validate
    [Tag]
    (db/tags-for-issues {:issue-ids [1]}))
