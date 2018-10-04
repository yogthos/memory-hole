(ns memory-hole.handlers.issues
  (:require [memory-hole.attachments :refer [upload-file!]]
            [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx reg-fx]]
            [memory-hole.routes :refer [navigate!]]
            [ajax.core :refer [DELETE GET POST PUT]]))

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
  (fn [_ [_ mode & params]]
    {:http (merge {:method GET
                   :success-event [:set-issues]}
                  (case mode
                    :all {:url "/api/issues"}
                    :recent {:url "/api/recent-issues"}
                    :most-viewed {:url "/api/issues-by-views/0/20"}
                    :tag {:url (str "/api/issues-by-tag/" (first params))}
                    :search {:method POST
                             :url "/api/search-issues"
                             :ajax-map {:params {:query (first params)
                                                 :limit 10
                                                 :offset 0}}}))}))

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


(reg-event-fx
 :view-issue
 (fn [_ [_ issue-id]]
   {:navigate (str "/issue/" issue-id)}))

(reg-event-fx
  :create-issue
  (fn [_ [_ {:keys [title summary detail group-id tags]}]]
    {:http {:method POST
            :url "/api/issue"
            :success-event [:view-issue]
            :ajax-map {:params {:title      title
                                :summary    summary
                                :detail     detail
                                :group-id group-id
                                :tags    (vec tags)}}}}))

(reg-event-db
 :issue-hints
 (fn [db [_ issue-hints]]
   (assoc db :issue-hints (atom issue-hints))))

(reg-event-fx
 :get-issue-hints
 (fn [_ [_ issue-prefix limit]]
   {:http {:method GET
           :url (str "/api/issues-by-content/" issue-prefix "?limit=" limit)
           :success-event [:issue-hints]}}))

(reg-event-fx
 :view-edited-issue
 (fn [_ [_ issue-id]]
   {:navigate (str "/issue/" issue-id)}))

(reg-event-fx
  :save-issue
  (fn [_ [_ {:keys [support-issue-id title summary group-id detail tags]}]]
    {:http {:method PUT
            :url "/api/issue"
            :success-event [:view-issue support-issue-id]
            :ignore-response-body true
            :ajax-map {:params {:support-issue-id support-issue-id
                                :title            title
                                :summary          summary
                                :detail           detail
                                :group-id         group-id
                                :tags             (vec tags)}}}}))

(reg-event-fx
 :navigate-to-home-page
 (fn [_ _]
   {:navigate "/"}))

(reg-event-fx
  :delete-issue
  (fn [_ [_ support-issue-id]]
    {:http {:method DELETE
            :url (str "/api/issue/" support-issue-id)
            :ignore-response-body true
            :success-event [:navigate-to-home-page]}}))

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
    {:http {:method DELETE
            :url (str "/api/file/" support-issue-id "/" filename)
            :ignore-response-body true
            :success-event [:remove-file-from-issue filename]}}))

