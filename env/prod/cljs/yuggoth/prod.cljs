(ns memory-hole.app
  (:require [memory-hole.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
