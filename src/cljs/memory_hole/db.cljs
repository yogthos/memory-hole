(ns memory-hole.db)

(def default-db
  {:user         (js->clj js/user :keywordize-keys true)
   :selected-tag "Recent"
   :active-page  (when-not js/user :login)
   :login-events []})
