(ns memory-hole.handlers.groups
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db]]
            [memory-hole.routes :refer [navigate!]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [DELETE GET POST PUT]]))

(reg-event-db
  :set-groups
  (fn [db [_ groups]]
    (assoc db :groups groups)))

(reg-event-db
  :load-all-groups
  (fn [db _]
    (GET "/api/groups"
         {:handler       #(dispatch [:set-groups (:groups %)])
          :error-handler #(ajax-error %)})
    db))
