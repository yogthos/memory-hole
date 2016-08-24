(ns memory-hole.datetime
  (:require [cljs-time.format :as f]
            [cljs-time.coerce :as c]))

(defn format-date [date]
  (f/unparse (f/formatter "dd-MM-yyyy")
             (c/from-date date)))