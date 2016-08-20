(ns yuggoth.routes
  (:require [re-frame.core :refer [dispatch subscribe]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [secretary.core :as secretary :include-macros true])
  (:import goog.History))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (dispatch [:set-active-page :home]))

(secretary/defroute "/issue/:id" [id]
  (if (subscribe [:issue])
    (dispatch [:set-active-page :view-issue])
    (dispatch [:load-and-view-issue (js/ParseInt id)])))



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
