(ns memory-hole.handlers.groups
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [memory-hole.routes :refer [navigate!]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [DELETE GET POST PUT]]))

(reg-event-db
  :set-groups
  (fn [db [_ {:keys [groups]}]]
    (assoc db :groups groups)))

(reg-event-fx
  :load-groups
  (fn [_ _]
    {:http {:method GET
            :url "/api/groups"
            :success-event [:set-groups]}}))
