(ns yuggoth.core
  (:require [reagent.core :as r]
            [yuggoth.routes :refer [hook-browser-navigation!]]
            [yuggoth.ajax :refer [load-interceptors!]]
            [yuggoth.views :refer [main-page]]
            [re-frame.core :refer [dispatch-sync]]
    ;;initialize handlers and subscriptions
            yuggoth.handlers
            yuggoth.subscriptions))

(defn mount-components []
  (r/render [#'main-page] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize-db])
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
