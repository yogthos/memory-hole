(ns memory-hole.handlers.admin
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [GET POST PUT]]))

(reg-event-db
  :admin/set-users
  (fn [db [_ users]]
    (assoc db :admin/users users)))

(reg-event-db
  :admin/search-for-users
  (fn [db [_ screenname]]
    (GET (str "/admin/users/" screenname)
         :handler #(dispatch [:admin/set-users (:users %)])
         :error-handler #(ajax-error %))
    db))

(reg-event-db
  :admin/update-user-profile
  (fn [db [_ user]]
    (PUT "/admin/user"
         {:params        user
          :error-handler #(ajax-error %)})
    db))

(reg-event-db
  :admin/create-user-profile
  (fn [db [_ user]]
    (POST "/admin/user"
          {:params        user
           :error-handler #(ajax-error %)})
    db))
