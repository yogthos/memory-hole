(ns yuggoth.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[yuggoth started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[yuggoth has shut down successfully]=-"))
   :middleware identity})
