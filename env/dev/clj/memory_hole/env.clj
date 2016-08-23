(ns memory-hole.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [memory-hole.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[memory_hole started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[memory_hole has shut down successfully]=-"))
   :middleware wrap-dev})
