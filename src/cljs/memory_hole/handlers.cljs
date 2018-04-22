(ns memory-hole.handlers
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [ajax.core :refer [DELETE GET POST PUT]]
            [memory-hole.db :as db]
            [clojure.string :as string]
            memory-hole.handlers.errors
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
 :navigate-to
 (fn [_ [_ url]]
   {:navigate url}))

(reg-event-fx
  :run-login-events
  (fn [{{events :login-events :as db} :db} _]
    {:dispatch-n events
     :db db}))

(reg-event-db
  :add-login-event
  (fn [db [_ event]]
    (update db :login-events conj event)))

(reg-event-fx
  :login
  (fn [_ [_ userid pass]]
    (if (or (string/blank? userid) (string/blank? pass))
      {:dispatch [:set-error "Username and password cannot be blank."]}
      {:http {:method POST
              :url "/api/login"
              :ajax-map {:params {:userid userid :pass pass}}
              :success-event [:handle-login]
              :error-event [:handle-login-error]}})))

(reg-event-fx
  :handle-login
  (fn [{:keys [db]} [_ {:keys [user]}]]
    {:dispatch-n (list [:run-login-events]
                       [:set-active-page :home])
     :db (assoc db :user user)}))

(reg-event-fx
  :handle-login-error
  (fn [_ _]
    {:dispatch [:set-error "Invalid username/password."]}))

(reg-event-db
 :logout-client
 (fn [db _]
   (dissoc db :user)))

(reg-event-fx
 :handle-logout
 (fn [_ _]
   {:reload-page true}))

(reg-event-fx
  :logout
  (fn [_ _]
    {:http {:method POST
            :url "/api/logout"
            :ignore-response-body true
            :success-event [:handle-logout]
            :error-event [:handle-logout]}
     :db db/default-db
     :set-user! nil}))

(reg-event-db
  :unset-loading
  (fn [db _]
    (dissoc db :loading? :error :should-be-loading?)))

(reg-event-db
 :set-loading-for-real-this-time
 (fn [{:keys [should-be-loading?] :as db} _]
   (if should-be-loading?
     (assoc db :loading? true)
     db)))

(reg-event-fx
  :set-loading
  (fn [{db :db} _]
    {:dispatch-later [{:ms 100 :dispatch [:set-loading-for-real-this-time]}]
     :db (-> db
            (assoc :should-be-loading? true)
            (dissoc :error))}))


