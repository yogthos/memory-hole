(ns yuggoth.routes.services.issues
  (:require [yuggoth.db.core :as db]
            [schema.core :as s]
            [ring.util.http-response :refer :all]
            [yuggoth.routes.services.common :refer [handler]])
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
   :summary                      s/Str
   :detail                       s/Str
   (s/optional-key :create-date) Date
   :delete-date                  (s/maybe Date)
   :update-date                  (s/maybe Date)
   :last-updated-by              s/Num
   :last-viewed-date             Date
   :views                        s/Num
   :created-by                   s/Num
   (s/optional-key :tags)        [s/Str]
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

(def TagResult
  {(s/optional-key :tag)  Tag
   (s/optional-key :error) s/Str})

(def TagsResult
  {(s/optional-key :tags)  [Tag]
   (s/optional-key :error) s/Str})

(handler tags []
  (ok {:tags (db/ranked-tags)}))

(handler add-tag! [m]
  (ok {:tag (merge m (db/create-tag<! m))}))

(handler recent-issues [limit]
  (ok {:issues (db/recently-viewed-issues {:limit limit})}))

(handler add-issue! [issue]
  (ok (db/create-issue-with-tags! issue)))

(handler update-issue! [issue]
  (ok (db/update-issue-with-tags! issue)))

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
