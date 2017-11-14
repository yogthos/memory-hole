(ns memory-hole.handlers
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [ajax.core :refer [DELETE GET POST PUT]]
            [memory-hole.db :as db]
            memory-hole.handlers.admin
            memory-hole.handlers.groups
            memory-hole.handlers.issues
            memory-hole.handlers.tags))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))

(reg-event-fx
  :run-login-events
  (fn [{:keys [db]} _]
    (doseq [event (:login-events db)]
      (dispatch event))))

(reg-event-db
  :add-login-event
  (fn [db [_ event]]
    (update db :login-events conj event)))

(reg-event-db
  :login
  (fn [db [_ user]]
    (dispatch [:run-login-events])
    (assoc db :user user)))

(reg-event-db
  :logout
  (fn [db _]
    (dissoc db :user)))

(reg-event-db
  :set-error
  (fn [db [_ error]]
    (assoc db :error error)))

(reg-event-db
  :clear-error
  (fn [db _]
    (dissoc db :error)))

(reg-event-db
  :unset-loading
  (fn [db _]
    (dissoc db :loading? :error)))

(reg-event-db
  :set-loading
  (fn [db _]
    (assoc db :loading? true
              :error false)))


