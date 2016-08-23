(ns yuggoth.routes
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

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (dispatch [:set-active-page :home]))

(secretary/defroute "/create-issue" []
  (if-not (logged-in?)
    (set-location! "#/")
    (do
      (dispatch-sync [:close-issue])
      (dispatch [:set-active-page :edit-issue]))))

(secretary/defroute "/edit-issue" []
  (if-not (or (logged-in?)
              (nil? @(subscribe [:issue])))
    (set-location! "#/")
    (dispatch [:set-active-page :edit-issue])))

(secretary/defroute "/view-issue" []
  (dispatch [:set-active-page :view-issue]))

(secretary/defroute "/issue/:id" [id]
  (cond
    (not (logged-in?))
    (dispatch [:add-login-event [:load-and-view-issue (js/parseInt id)]])
    @(subscribe [:issue])
    (dispatch [:set-active-page :view-issue])
    :else
    (dispatch [:load-and-view-issue (js/parseInt id)])))

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
