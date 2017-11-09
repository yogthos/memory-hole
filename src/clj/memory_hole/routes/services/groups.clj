(ns memory-hole.routes.services.groups
  (:require [memory-hole.db.core :as db]
            [memory-hole.routes.services.common :refer [handler]]
            [memory-hole.validation :as v]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [ring.util.http-response :refer :all])
  (:import java.util.Date))

(def Group
  {:group-id                     s/Num
   :group-name                   s/Str
   (s/optional-key :create-date) Date})

(def GroupResult
  {(s/optional-key :group) Group
   (s/optional-key :error)      s/Str})

(def GroupsResult
  {(s/optional-key :groups) [Group]
   (s/optional-key :error)      s/Str})

(handler groups []
         (ok {:groups (db/groups)}))

(handler add-group! [group]
  (if-let [errors (v/validate-group group [])]
    (do
      (log/error "error updating group:" group)
      (bad-request {:error "invalid group"}))
    (ok {:group (merge group (db/create-group<! group))})))
