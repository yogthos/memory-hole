(ns user
  (:require [mount.core :as mount]
            [memory-hole.figwheel :refer [start-fw stop-fw cljs]]
            memory-hole.core))

(defn start []
  (mount/start-without #'memory-hole.core/repl-server))

(defn stop []
  (mount/stop-except #'memory-hole.core/repl-server))

(defn restart []
  (stop)
  (start))


