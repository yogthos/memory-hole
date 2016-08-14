(ns yuggoth.db)

(def default-db
  {:user        (js->clj js/user)
   :active-page (if js/user :home :login)})