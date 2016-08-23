(ns yuggoth.db)

(def default-db
  {:user         (js->clj js/user :keywordize-keys true)
   :selected-tag "Recent"
   :active-page  (if js/user :home :login)
   :login-events [[:load-tags]
                  [:load-recent-issues]]})