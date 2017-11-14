(ns memory-hole.handlers.issues
  (:require [memory-hole.attachments :refer [upload-file!]]
            [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx reg-fx]]
            [memory-hole.routes :refer [navigate!]]
            [memory-hole.ajax :refer [ajax-error]]
            [ajax.core :refer [DELETE GET POST PUT]]))

(reg-event-fx
 :ajax-error
 (fn [_ [_ {status :status {error :error} :response}]]
   {:dispatch (if (= 401 status)
                [:logout]
                [:set-error error])}))
(reg-event-db
  :set-issue
  (fn [db [_ {issue :issue}]]
    (assoc db :issue issue)))

(reg-event-db
  :close-issue
  (fn [db _]
    (dissoc db :issue)))

(reg-event-db
  :set-issues
  (fn [db [_ {issues :issues}]]
    (assoc db :issues issues)))

(reg-event-fx
  :load-issues
  (fn [_ [_ mode]]
    {:http {:method GET
            :url (case mode
                   :all "/api/issues"
                   :recent "/api/recent-issues"
                   :most-viewed "/api/issues-by-views/0/20")
            :success-event [:set-issues]}}))

(reg-event-fx
  :load-issues-for-tag
  (fn [_ [_ tag]]
    {:http {:method GET
            :url (str "/api/issues-by-tag/" tag)
            :success-event [:set-issues]}}))

(reg-event-fx
  :search-for-issues
  (fn [_ [_ query]]
    {:http {:method POST
            :url "/api/search-issues"
            :success-event [:set-issues]
            :ajax-map {:params {:query  query
                                :limit  10
                                :offset 0}}}}))


(reg-event-db
 :handle-issue-load
 (fn [db [_ {issue :issue}]]
   (-> db
       (assoc :issue issue)
       (assoc :active-page :view-issue))))

(reg-event-fx
 :close-issue-and-error
 (fn [_ [_ error]]
   {:dispatch-n (list [:close-issue] [:ajax-error error])}))

(reg-event-fx
  :load-issue
  (fn [_ [_ support-issue-id]]
    {:http {:method GET
            :url (str "/api/issue/" support-issue-id)
            :success-event [:set-issue]
            :error-event [:close-issue-and-error]}}))


;; REFACTOR INTO HTTP FX
(reg-event-db
  :create-issue
  (fn [db [_ {:keys [title summary detail group-name tags] :as issue}]]
    (POST "/api/issue"
          {:params        {:title      title
                           :summary    summary
                           :detail     detail
                           :group-name group-name
                           :tags    (vec tags)}
           :handler       #(do
                            (dispatch [:load-tags] tags)
                            (dispatch-sync [:set-issue (assoc issue :support-issue-id %)])
                            (navigate! (str "/issue/" %)))
           :error-handler #(ajax-error %)})
    nil))

;; REFACTOR INTO HTTP FX
(reg-event-db
  :save-issue
  (fn [db [_ {:keys [support-issue-id title summary group-name detail tags] :as issue}]]
    (PUT "/api/issue"
         {:params        {:support-issue-id support-issue-id
                          :title            title
                          :summary          summary
                          :detail           detail
                          :group-name       group-name
                          :tags             (vec tags)}
          :handler       #(do
                           (dispatch [:load-tags] tags)
                           (dispatch-sync [:set-issue issue])
                           (navigate! (str "/issue/" support-issue-id)))
          :error-handler #(ajax-error %)})
    nil))

;; REFACTOR INTO HTTP FX
(reg-event-db
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

;; REFACTOR INTO HTTP FX
(reg-event-db
  :delete-file
  (fn [_ [_ support-issue-id filename]]
    (DELETE (str "/api/file/" support-issue-id "/" filename)
            {:handler #(dispatch [:remove-file-from-issue filename])
             :error-handler #(ajax-error %)})
    nil))

