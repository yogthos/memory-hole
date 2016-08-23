(ns memory-hole.core
  (:require [reagent.core :as r]
            [memory-hole.routes :refer [hook-browser-navigation!]]
            [memory-hole.ajax :refer [load-interceptors!]]
            [memory-hole.views :refer [main-page]]
            [memory-hole.pages.auth :refer [logged-in?]]
            [re-frame.core :refer [dispatch dispatch-sync]]
    ;;initialize handlers and subscriptions
            memory-hole.handlers
            memory-hole.subscriptions))

(defn mount-components []
  (r/render [#'main-page] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (if (logged-in?) (dispatch [:run-login-events]))
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
