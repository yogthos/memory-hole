(ns memory-hole.subscriptions
  (:require [re-frame.core :refer [reg-sub reg-sub-raw subscribe]]
            [re-frame.interop :refer [make-reaction]]))

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

(reg-sub :issue-hints query)

(reg-sub :get-issue-hints query)

(reg-sub :issues query)

(reg-sub :error query)

(reg-sub :login-events query)

(reg-sub
 :groups
 (fn [db _]
   (distinct (:groups db))))

(reg-sub
 :belongs-to
 :<-[:user]
 (fn [user _]
   (:belongs-to user)))

(reg-sub
 :visible-issues
 :<-[:issues]
 (fn [issues _]
   issues))


;;admin
(reg-sub :admin/users query)

(reg-sub
  :admin/group-users
  (fn [db [_ group-name]]
    (get-in db [:group-users group-name])))
