(ns memory-hole.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(def errors
  {[:title]   "issue title is required"
   [:summary] "issue summary is required"
   [:detail]  "issue details are required"
   [:tags]    "issue must have at least one tag"})


(defn format-validation-errors [errors]
  (->> errors
       first
       (map (fn [[k [v]]] [k v]))
       (into {})
       not-empty))

(defn validate-issue [issue]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        (errors path))
      issue
      :title v/required
      :summary v/required
      :detail v/required
      :tags [v/required [v/min-count 1]])))



