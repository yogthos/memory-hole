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
        ({[:title]   "Issue title is required"
          [:summary] "Issue summary is required"
          [:detail]  "Issue details are required"
          [:tags]    "Issue must have at least one tag"}
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
        ({[:screenname]   "Screenname is required"
          [:pass]         "Password of 8+ characters is required"
          [:pass-confirm] "Password confirmation doesn't match"
          [:is-admin]     "You must specify whether the user is an admin"
          [:active]       "You must pecify whether the user is active"}
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
        ({[:screenname]   "Screenname is required"
          [:is-admin]     "You must specify whether the user is an admin"
          [:pass-confirm] "Password confirmation doesn't match"
          [:active]       "You must pecify whether the user is active"}
          path))
      user
      :screenname v/required
      :admin v/required
      :pass-confirm [[pass-matches? (:pass user)]]
      :is-active v/required)))
