(ns memory-hole.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [secretary.core :as secretary])
  (:import goog.History))

(defn url [parts]
  (if-let [context (not-empty js/context)]
    (apply (partial str context "/") parts)
    (apply str parts)))

(defn set-location! [& url-parts]
  (set! (.-href js/location) (url url-parts)))

(defn logged-in? []
  @(subscribe [:user]))

(defn run-events [events]
  (doseq [event events]
    (if (logged-in?)
      (dispatch event)
      (dispatch [:add-login-event event]))))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (run-events [[:load-tags]
                [:load-recent-issues]
                [:set-active-page :home]]))

(secretary/defroute "/issues/:tag" [tag]
  (run-events
    [[:select-tag tag]
     [:load-issues-for-tag tag]]))

(secretary/defroute "/create-issue" []
  (dispatch-sync [:close-issue])
  (run-events
    [:set-active-page :edit-issue]))

(secretary/defroute "/edit-issue" []
  (if-not (or (logged-in?)
              (nil? @(subscribe [:issue])))
    (set-location! "#/")
    (dispatch [:set-active-page :edit-issue])))

(secretary/defroute "/issue/:id" [id]
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
    (.setEnabled true)))
