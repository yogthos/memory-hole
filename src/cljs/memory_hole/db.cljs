(ns memory-hole.db)

(def default-db
  ;; user should be coeffect on init event, not hard coded as it is here
  {:user                   (js->clj js/user :keywordize-keys true)
   :selected-tag           "Recent"
   :admin/show-all-groups? false
   :active-page            (when-not js/user :login)
   :login-events           []})
