(ns memory-hole.handlers.admin
  (:require [re-frame.core :as rf :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [ajax.core :refer [GET POST PUT]]))


;;users

(reg-event-db
  :admin/set-users
  (fn [db [_ {:keys [users]}]]
    (assoc db :admin/users users)))

(reg-event-fx
  :admin/search-for-users
  (fn [_ [_ screenname]]
    {:http {:method GET
            :url (str "/admin/users/" screenname)
            :success-event [:admin/set-users]}}))

(reg-event-db
  :admin/set-user-info
  (fn [db [_ {{:keys [user-id] :as user} :user}]]
    (update db :admin/users
            (fn [users]
              (map #(if (= user-id (:user-id %)) user %) users)))))

(reg-event-fx
  :admin/update-user-profile
  (fn [_ [_ user]]
    {:http {:method PUT
            :url "/admin/user"
            :success-event [:admin/set-user-info]
            :ajax-map {:params user}
            }}))

(reg-event-fx
  :admin/create-user-profile
  (fn [_ [_ user]]
    {:http {:method POST
            :url "/admin/user"
            :ajax-map {:params user}}}))

;;groups
(reg-event-db
  :admin/add-group-info
  (fn [db [_ {:keys [group]}]]
    (update db :groups #(conj % group))))

(reg-event-fx
  :admin/create-group
  (fn [_ [_ group]]
    {:http {:method POST
            :url "/admin/group"
            :success-event [:admin/add-group-info]
            :ajax-map {:params group}}}))

(reg-event-db
  :admin/set-group-users
  (fn [db [_ group-name users]]
    (assoc-in db [:group-users group-name] users)))

(reg-event-db
  :admin/load-group-users
  (fn [db [_ group-name]]
    (GET (str "/admin/users/group/" group-name)
         :handler #(dispatch [:admin/set-group-users group-name (:users %)])
         :error-handler #(ajax-error %))
    db))

(reg-event-db
  :admin/set-group-users
  (fn [db [_ group-name users]]
    (assoc-in db [:group-users group-name] users)))

(reg-event-db
  :admin/load-group-users
  (fn [db [_ group-name]]
    (GET (str "/admin/users/group/" group-name)
         :handler #(dispatch [:admin/set-group-users group-name (:users %)])
         :error-handler #(ajax-error %))
    db))
