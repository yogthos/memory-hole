(ns yuggoth.handlers
  (:require [re-frame.core :refer [dispatch reg-event-db]]
            [ajax.core :refer [GET POST]]
            [yuggoth.db :as db]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))

(reg-event-db
  :login
  (fn [db [_ user]]
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
  :set-active-issue
  (fn [db [_ issue]]
    (assoc db :active-page :edit
              :loading? false
              :issue issue)))

(defn loading [db]
  (assoc db :loading? true
            :error false))

(reg-event-db
  :edit-issue
  (fn [db [_ issue-id]]
    (GET (str "/api/issue/" issue-id)
         {:handler       #(dispatch [:set-active-issue (:issue %)])
          :error-handler #(dispatch [:set-error (str %)])})
    (loading db)))

(reg-event-db
  :process-issue-save
  (fn [db issue]
    (assoc db :active-page :view-issue
              :loading? false
              :issue issue)))

(reg-event-db
  :save-issue
  (fn [db [_ issue]]
    (assoc db :active-page :home
              :issue issue))
  #_(fn [db [_ issue]]
      (POST "/api/save"
            {:params        {:issue issue}
             :handler       #()
             :error-handler #(dispatch [:set-error (str %)])})
      (loading db)))

(reg-event-db
  :cancel-issue-edit
  (fn [db _]
    (assoc db :active-page :home)))
