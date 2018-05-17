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
          [:group-id] "Issue must belong to a group"
          [:tags]    "Issue must have at least one tag"}
          path))
      issue
      :title v/required
      :summary v/required
      :detail v/required
      :group-id v/required
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
          [:belongs-to]   "Must belong to at least one group"
          [:is-admin]     "You must specify whether the user is an admin"
          [:active]       "You must specify whether the user is active"}
          path))
      user
      :pass [v/required [v/min-count 8]]
      :pass-confirm [[pass-matches? (:pass user)]]
      :screenname v/required
      :admin v/required
      :belongs-to [v/required [v/min-count 1]]
      :is-active v/required)))

(defn validate-update-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:screenname]   "Screenname is required"
          [:is-admin]     "You must specify whether the user is an admin"
          [:belongs-to]   "Must belong to at least one group"
          [:pass-confirm] "Password confirmation doesn't match"
          [:active]       "You must specify whether the user is active"}
          path))
      user
      :screenname v/required
      :admin v/required
      :pass-confirm [[pass-matches? (:pass user)]]
      :belongs-to [v/required [v/min-count 1]]
      :is-active v/required)))

(defn group-unique? [group-name other-groups]
  (every? #(not= group-name %) other-groups))

(defn validate-group [group other-groups]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:group-name] "Group name is required and must be unique"
          [:group-id] "Group id is optional"}
          path))
      group
      :group-name [v/required
                   [v/min-count 1]
                   [group-unique? other-groups]])))
