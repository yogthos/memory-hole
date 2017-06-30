(ns memory-hole.handlers.issues
  (:require [memory-hole.attachments :refer [upload-file!]]
            [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx]]
            [memory-hole.routes :refer [navigate!]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [DELETE GET POST PUT]]))

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

(reg-event-fx
  :load-all-issues
  (fn [_ _]
    (GET "/api/issues"
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :load-recent-issues
  (fn [_ _]
    (GET "/api/recent-issues"
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :load-most-viewed-issues
  (fn [_ _]
    (GET "/api/issues-by-views/0/20"
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :load-issues-for-tag
  (fn [_ [_ tag]]
    (GET (str "/api/issues-by-tag/" tag)
         {:handler       #(dispatch [:set-issues (:issues %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :search-for-issues
  (fn [_ [_ query]]
    (POST "/api/search-issues"
          {:params        {:query  query
                           :limit  10
                           :offset 0}
           :handler       #(dispatch [:set-issues (:issues %)])
           :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :load-issue-detail
  (fn [_ [_ support-issue-id]]
    (GET (str "/api/issue/" support-issue-id)
         {:handler       #(dispatch [:set-issue (:issue %)])
          :error-handler #(ajax-error %)})
    nil))

(reg-event-db
  :load-and-view-issue
  (fn [db [_ support-issue-id]]
    (GET (str "/api/issue/" support-issue-id)
         {:handler       #(do
                           (dispatch-sync [:set-issue (:issue %)])
                           (dispatch [:set-active-page :view-issue]))
          :error-handler #(ajax-error %)})
    (dissoc db :issue)))

(reg-event-db
  :process-issue-save
  (fn [db issue]
    (assoc db :active-page :view-issue
              :issue issue)))

(reg-event-fx
  :create-issue
  (fn [_ [_ {:keys [title summary detail tags] :as issue}]]
    (POST "/api/issue"
          {:params        {:title   title
                           :summary summary
                           :detail  detail
                           :tags    (vec tags)}
           :handler       #(do
                            (dispatch [:load-tags] tags)
                            (dispatch-sync [:set-issue (assoc issue :support-issue-id %)])
                            (navigate! (str "/issue/" %)))
           :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :save-issue
  (fn [_ [_ {:keys [support-issue-id title summary detail tags] :as issue}]]
    (PUT "/api/issue"
         {:params        {:support-issue-id support-issue-id
                          :title            title
                          :summary          summary
                          :detail           detail
                          :tags             (vec tags)}
          :handler       #(do
                           (dispatch [:load-tags] tags)
                           (dispatch-sync [:set-issue issue])
                           (navigate! (str "/issue/" support-issue-id)))
          :error-handler #(ajax-error %)})
    nil))

(reg-event-fx
  :delete-issue
  (fn [_ [_ support-issue-id]]
    (DELETE (str "/api/issue/" support-issue-id)
            {:handler       #(navigate! "/")
             :error-handler #(ajax-error %)})
    nil))

(reg-event-db
  :attach-file
  (fn [db [_ filename]]
    (update-in db [:issue :files] conj filename)))

(reg-event-db
  :remove-file-from-issue
  (fn [db [_ filename]]
    (update-in db [:issue :files] #(remove #{filename} %))))

(reg-event-fx
  :delete-file
  (fn [_ [_ support-issue-id filename]]
    (DELETE (str "/api/file/" support-issue-id "/" filename)
            {:handler #(dispatch [:remove-file-from-issue filename])
             :error-handler #(ajax-error %)})
    nil))

