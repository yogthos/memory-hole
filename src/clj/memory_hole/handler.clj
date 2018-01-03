(ns memory-hole.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [memory-hole.layout :refer [error-page]]
            [memory-hole.routes.home :refer [home-routes]]
            [memory-hole.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [memory-hole.env :refer [defaults]]
            [mount.core :as mount]
            [memory-hole.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (wrap-routes #'home-routes middleware/wrap-csrf)
      service-routes
      (route/not-found
        (:body
          (error-page {:status 404
                       :title  "page not found"}))))))
