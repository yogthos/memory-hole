(ns memory-hole.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

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
        ({[:title]   "issue title is required"
          [:summary] "issue summary is required"
          [:detail]  "issue details are required"
          [:tags]    "issue must have at least one tag"}
          path))
      issue
      :title v/required
      :summary v/required
      :detail v/required
      :tags [v/required [v/min-count 1]])))

(defn validate-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:screenname] "screenname is required"
          [:pass] "password is required"
          [:pass-confirm] "confirmation password must match"
          [:is-admin] "must specify whether the user is an admin"
          [:active] "specify whether the user is active"}
          path))
      user
      :screenname v/required
      :pass v/required
      :pass-confirm v/required
      :is-admin v/required
      :active v/required)))
