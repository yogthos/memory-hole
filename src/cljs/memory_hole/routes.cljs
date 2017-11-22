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

;; Why do you need to scrollTo on these events?
(defn home-page-events [& events]
  (.scrollTo js/window 0 0)
  (run-events (into
               [[:load-tags]
                [:load-groups]
                [:set-active-page :home]]
                events)))

;; -------------------------
;; Routes
(secretary/defroute (context-url "/") []
  (home-page-events [:load-issues :recent]))

(secretary/defroute (context-url "/search/:query") [query]
  (home-page-events [:load-issues :search query]))

(secretary/defroute (context-url "/all-issues") []
  (home-page-events [:select-tag "All"]
                    [:load-issues :all]))

(secretary/defroute (context-url "/recent-issues") []
  (home-page-events [:select-tag "Recent"]
                    [:load-issues :recent]))

(secretary/defroute (context-url "/most-viewed-issues") []
  (home-page-events [:select-tag "Most Viewed"]
                    [:load-issues :most-viewed]))

(secretary/defroute (context-url "/issues/:tag") [tag]
  (home-page-events [:select-tag tag]
                    [:load-issues :tag tag]))

(secretary/defroute (context-url "/create-issue") []
  (dispatch [:close-issue])
  (run-events
   [[:load-tags]
    [:load-groups]
    [:set-active-page :edit-issue]]))

(secretary/defroute (context-url "/edit-issue") []
  (if-not (or (logged-in?)
              (nil? @(subscribe [:issue])))
    (navigate! "/")
    (run-events [[:load-tags]
                 [:load-groups]
                 [:set-active-page :edit-issue]])))

(secretary/defroute (context-url "/issue/:id") [id]
  (dispatch [:close-issue])
  (run-events [[:load-tags]
               [:load-groups]
               [:load-issue (js/parseInt id)]
               [:set-active-page :view-issue]]))

(secretary/defroute (context-url "/users") []
  (run-events [[:load-groups]
               [:set-active-page :users]]))

(secretary/defroute (context-url "/groups") []
  (run-events [[:load-groups]
               [:set-active-page :groups]]))

;; Consider adding a group-detail page for batch removing/adding users to groups

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
