(ns memory-hole.handlers.errors
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]))

(reg-event-fx
 :ajax-error
 (fn [_ [_ {status :status {error :error} :response}]]
   {:dispatch (if (= 401 status)
                [:logout-client]
                [:set-error error])}))

(reg-event-db
 :set-error
 (fn [db [_ error]]
   (assoc db :error error)))

(reg-event-db
 :clear-error
 (fn [db _]
   (dissoc db :error)))
