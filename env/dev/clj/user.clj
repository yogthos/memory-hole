(ns user
  (:require [mount.core :as mount]
            [yuggoth.figwheel :refer [start-fw stop-fw cljs]]
            yuggoth.core))

(defn start []
  (mount/start-without #'yuggoth.core/repl-server))

(defn stop []
  (mount/stop-except #'yuggoth.core/repl-server))

(defn restart []
  (stop)
  (start))


