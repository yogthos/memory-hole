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

(defn pass-matches? [pass-confirm pass]
  (= pass pass-confirm))

(defn validate-create-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:screenname]   "screenname is required"
          [:pass]         "password of 8+ characters is required"
          [:pass-confirm] "confirmation password doesn't match"
          [:is-admin]     "must specify whether the user is an admin"
          [:active]       "specify whether the user is active"}
          path))
      user
      :pass [v/required [v/min-count 8]]
      :pass-confirm [[pass-matches? (:pass user)]]
      :screenname v/required
      :admin v/required
      :is-active v/required)))

(defn validate-update-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:screenname] "screenname is required"
          [:is-admin]   "must specify whether the user is an admin"
          [:pass-confirm] "confirmation password doesn't match"
          [:active]     "specify whether the user is active"}
          path))
      user
      :screenname v/required
      :admin v/required
      :pass-confirm [[pass-matches? (:pass user)]]
      :is-active v/required)))
