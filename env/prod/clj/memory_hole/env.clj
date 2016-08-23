(ns memory-hole.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[memory_hole started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[memory_hole has shut down successfully]=-"))
   :middleware identity})
