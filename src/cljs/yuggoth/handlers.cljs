(ns yuggoth.handlers
  (:require [re-frame.core :refer [register-handler]]
            [yuggoth.db :as db]))

(register-handler
  :initialize-db
  (fn  [_ _]
    db/default-db))

(register-handler
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))