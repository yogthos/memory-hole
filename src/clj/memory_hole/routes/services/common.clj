(ns memory-hole.routes.services.common
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :refer [internal-server-error]]))

(defmacro handler [fn-name args & body]
  `(defn ~fn-name ~args
     (try
       ~@body
       (catch Throwable t#
         (log/error t# "error handling request")
         (internal-server-error {:error "error occured serving the request"})))))
