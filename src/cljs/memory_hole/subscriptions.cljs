(ns memory-hole.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :db-state
  (fn [db _]
    db))

(defn query [db [event-id]]
  (event-id db))

(reg-sub :active-page query)

(reg-sub :loading? query)

(reg-sub :user query)

(reg-sub :tags query)

(reg-sub :selected-tag query)

(reg-sub :issue query)

(reg-sub :issues query)

(reg-sub :error query)

(reg-sub :login-events query)

;;admin
(reg-sub :admin/users query)

