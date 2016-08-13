(ns yuggoth.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [yuggoth.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[yuggoth started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[yuggoth has shut down successfully]=-"))
   :middleware wrap-dev})
