(ns memory-hole.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [secretary.core :as secretary]
            [accountant.core :as accountant])
  (:import goog.History))

(defn logged-in? []
  @(subscribe [:user]))

(defn run-events [events]
  (doseq [event events]
    (if (logged-in?)
      (dispatch event)
      (dispatch [:add-login-event event]))))

(defn context-url [url]
  (str js/context url))

(defn href [url]
  {:href (str js/context url)})

(defn navigate! [url]
  (accountant/navigate! (context-url url)))

(defn home-page-events [& events]
  (.scrollTo js/window 0 0)
  (run-events (into
                [[:load-tags]
                 [:set-active-page :home]]
                events)))

;; -------------------------
;; Routes
(secretary/defroute (context-url "/") []
  (home-page-events [:load-recent-issues]))

(secretary/defroute (context-url "/search/:query") [query]
  (home-page-events [:search-for-issues query]))

(secretary/defroute (context-url "/all-issues") []
  (home-page-events [:select-tag "All"]
                    [:load-all-issues]))

(secretary/defroute (context-url "/recent-issues") []
  (home-page-events [:select-tag "Recent"]
                    [:load-recent-issues]))

(secretary/defroute (context-url "/most-viewed-issues") []
  (home-page-events [:select-tag "Most Viewed"]
                    [:load-most-viewed-issues]))

(secretary/defroute (context-url "/issues/:tag") [tag]
  (home-page-events [:select-tag tag]
                    [:load-issues-for-tag tag]))

(secretary/defroute (context-url "/create-issue") []
  (dispatch-sync [:close-issue])
  (run-events
    [[:load-tags]
     [:set-active-page :edit-issue]]))

(secretary/defroute (context-url "/edit-issue") []
  (if-not (or (logged-in?)
              (nil? @(subscribe [:issue])))
    (navigate! "/")
    (dispatch [:set-active-page :edit-issue])))

(secretary/defroute (context-url "/issue/:id") [id]
  (run-events [[:load-tags]
               [:load-and-view-issue (js/parseInt id)]]))

(secretary/defroute (context-url "/users") []
  (run-events [[:set-active-page :users]]))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true))
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!))
