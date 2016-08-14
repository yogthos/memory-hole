(ns yuggoth.handlers
  (:require [re-frame.core :refer [register-handler]]
            [yuggoth.db :as db]))

(register-handler
  :initialize-db
  (fn [_ _]
    (let [user (when js/user (js->clj js/user))]
      (assoc
        db/default-db
        :user user
        :active-page (if user :home :login)))))

(register-handler
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))

(register-handler
  :login
  (fn [db [_ user]]
    (assoc db :user user)))

(register-handler
  :logout
  (fn [db [_ user]]
    (dissoc db :user)))