(ns memory-hole.handlers.tags
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [ajax.core :refer [GET POST]]))

(reg-event-db
  :set-tags
  (fn [db [_ {:keys [tags]}]]
    (assoc db :tags tags)))

(reg-event-db
  :select-tag
  (fn [db [_ tag]]
    (assoc db :selected-tag tag)))

(reg-event-fx
  :load-tags
  (fn [_ _]
    {:http {:method GET
            :url "/api/tags"
            :success-event [:set-tags]}}))




