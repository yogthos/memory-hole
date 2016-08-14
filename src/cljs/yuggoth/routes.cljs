(ns yuggoth.routes
  (:require [re-frame.core :refer [dispatch]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [secretary.core :as secretary :include-macros true])
  (:import goog.History))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (dispatch [:set-active-page :home]))

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
