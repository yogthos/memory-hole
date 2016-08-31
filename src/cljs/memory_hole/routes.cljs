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

;; -------------------------
;; Routes
(secretary/defroute (context-url "/") []
  (run-events [[:load-tags]
               [:load-recent-issues]
               [:set-active-page :home]]))

(secretary/defroute (context-url "/search/:query") [query]
  (.scrollTo js/window 0 0)
  (run-events [[:search-for-issues query]]))

(secretary/defroute (context-url "/users") []
  (run-events [[:set-active-page :users]]))

(secretary/defroute (context-url "/issues/:tag") [tag]
  (run-events
    [[:select-tag tag]
     [:load-issues-for-tag tag]]))

(secretary/defroute (context-url "/create-issue") []
  (dispatch-sync [:close-issue])
  (run-events
    [[:set-active-page :edit-issue]]))

(secretary/defroute (context-url "/edit-issue") []
  (if-not (or (logged-in?)
              (nil? @(subscribe [:issue])))
    (navigate! "/")
    (dispatch [:set-active-page :edit-issue])))

(secretary/defroute (context-url "/issue/:id") [id]
  (run-events [[:load-and-view-issue (js/parseInt id)]]))

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
