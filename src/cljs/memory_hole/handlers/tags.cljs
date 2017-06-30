(ns memory-hole.handlers.tags
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [GET POST]]))

(reg-event-db
  :set-tags
  (fn [db [_ tags]]
    (assoc db :tags tags)))

(reg-event-db
  :select-tag
  (fn [db [_ tag]]
    (assoc db :selected-tag tag)))

(reg-event-fx
  :load-tags
  (fn [_ _]
    (GET "/api/tags"
         {:handler       #(dispatch [:set-tags (:tags %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-db
  :add-tag
  (fn [db [_ tag]]
    (update db :tags conj tag)))

(reg-event-fx
  :create-tag
  (fn [_ [_ tag]]
    (POST "/api/tag"
          {:params        {:tag tag}
           :handler       #(dispatch [:add-tag %])
           :error-handler #(ajax-error %)})
    nil))
