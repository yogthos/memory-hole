(ns memory-hole.handlers.admin
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [GET POST PUT]]))

(reg-event-db
  :admin/set-users
  (fn [db [_ users]]
    (assoc db :admin/users users)))

(reg-event-fx
  :admin/search-for-users
  (fn [_ [_ screenname]]
    (GET (str "/admin/users/" screenname)
         :handler #(dispatch [:admin/set-users (:users %)])
         :error-handler #(ajax-error %))
    nil))

(reg-event-db
  :admin/set-user-info
  (fn [db [_ {:keys [user-id] :as user}]]
    (update db :admin/users
            (fn [users]
              (map #(if (= user-id (:user-id %)) user %) users)))))

(reg-event-fx
  :admin/update-user-profile
  (fn [_ [_ user]]
    (PUT "/admin/user"
         {:params        user
          :handler       #(dispatch [:admin/set-user-info (:user %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :admin/create-user-profile
  (fn [_ [_ user]]
    (POST "/admin/user"
          {:params        user
           :error-handler #(ajax-error %)})
    nil))
