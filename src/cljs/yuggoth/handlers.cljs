(ns yuggoth.handlers
  (:require [re-frame.core :refer [reg-event-db]]
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
  (fn [db [_ user]]
    (dissoc db :user)))