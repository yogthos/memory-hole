(ns yuggoth.handlers
  (:require [re-frame.core :refer [dispatch dispatch-sync reg-event-db]]
            [secretary.core :as secretary]
            [ajax.core :refer [GET POST PUT]]
            [yuggoth.db :as db]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))

(reg-event-db
  :run-login-events
  (fn [db _]
    (doseq [event (:login-events db)]
      (dispatch event))
    db))

(reg-event-db
  :add-login-event
  (fn [db [_ event]]
    (update db :login-events conj event)))

(reg-event-db
  :login
  (fn [db [_ user]]
    (dispatch [:run-login-events])
    (assoc db :user user)))

(reg-event-db
  :logout
  (fn [db _]
    (dissoc db :user)))

(reg-event-db
  :set-error
  (fn [db [_ error]]
    (assoc db :error error)))

(reg-event-db
  :clear-error
  (fn [db _]
    (dissoc db :error)))

(reg-event-db
  :unset-loading
  (fn [db _]
    (dissoc db :loading? :error)))

(reg-event-db
  :set-loading
  (fn [db _]
    (assoc db :loading? true
              :error false)))

;;tag event handlers
(reg-event-db
  :set-tags
  (fn [db [_ tags]]
    (assoc db :tags tags)))

(reg-event-db
  :load-tags
  (fn [db _]
    (GET "/api/tags"
         {:handler       #(dispatch [:set-tags (:tags %)])
          :error-handler #(dispatch [:set-error (str %)])})
    db))

;;issue event handlers
(reg-event-db
  :set-issue
  (fn [db [_ issue]]
    (assoc db :issue issue)))

(reg-event-db
  :close-issue
  (fn [db _]
    (dissoc db :issue)))

(reg-event-db
  :set-issues
  (fn [db [_ issues]]
    (assoc db :issues issues)))

(reg-event-db
  :load-recent-issues
  (fn [db _]
    (GET "/api/recent-issues"
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :load-issues-for-tag
  (fn [db [_ tag]]
    (GET (str "/api/issues-by-tag/" tag)
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :search-for-issues
  (fn [db [_ query]]
    (POST "/api/search-issues"
          {:params        {:query  query
                           :limit  10
                           :offset 0}
           :handler       #(dispatch [:set-issues (:issues %)])
           :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :load-issue-detail
  (fn [db [_ support-issue-id]]
    (GET (str "/api/issue/" support-issue-id)
         {:handler       #(dispatch [:set-issue (:issue %)])
          :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :load-and-view-issue
  (fn [db [_ support-issue-id]]
    (GET (str "/api/issue/" support-issue-id)
         {:handler       #(do
                           (dispatch-sync [:set-issue (:issue %)])
                           (dispatch [:set-active-page :view-issue]))
          :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :process-issue-save
  (fn [db issue]
    (assoc db :active-page :view-issue
              :issue issue)))

(reg-event-db
  :create-issue
  (fn [db [_ {:keys [title summary detail] :as issue}]]
    (POST "/api/issue"
          {:params        {:title title
                           :summary summary
                           :detail detail}
           :handler       #(do
                            (dispatch-sync [:set-issue (assoc issue :support-issue-id %)])
                            (secretary/dispatch! (str "/issue/" %)))
           :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :save-issue
  (fn [db [_ {:keys [support-issue-id title summary detail] :as issue}]]
    (PUT "/api/issue"
         {:params        {:support-issue-id support-issue-id
                          :title title
                          :summary summary
                          :detail detail}
          :handler       #(do
                           (dispatch-sync [:set-issue issue])
                           (secretary/dispatch! (str "/issue/" support-issue-id)))
          :error-handler #(dispatch [:set-error (str %)])})
    db))

(reg-event-db
  :cancel-issue-edit
  (fn [db _]
    (assoc db :active-page :view-issue)))
