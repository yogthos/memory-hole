(ns memory-hole.routes.services.auth
  (:require [memory-hole.config :refer [env]]
            [memory-hole.db.core :as db]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]
            [clj-ldap.client :as client]
            [schema.core :as s]
            [clojure.set :refer [rename-keys]]
            [ring.util.http-response :refer :all]))

(defstate host :start (:ldap env))
(defstate ldap-pool :start (when host (client/connect host)))

(defn authenticate [userid pass]
  (let [conn           (client/get-connection ldap-pool)
        qualified-name (str userid "@" (-> host :host :address))]
    (try
      (when (client/bind? conn qualified-name pass)
        (-> (client/search conn
                           (-> env :ldap :dc)
                           {:filter     (str "sAMAccountName=" userid)
                            :attributes [:displayName
                                         :memberOf
                                         :sAMAccountName]})
            first
            (select-keys [:displayName :memberOf :sAMAccountName])
            (rename-keys
              {:displayName    :screenname
               :memberOf       :member-of
               :sAMAccountName :account-name})))
      (finally (client/release-connection ldap-pool conn)))))

(def User
  {:user-id        s/Int
   :member-of      [(s/maybe s/Str)]
   :screenname     (s/maybe s/Str)
   :account-name   (s/maybe s/Str)
   :admin          s/Bool
   :is-active      s/Bool
   :client-ip      s/Str
   :source-address s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(defn login [userid pass {:keys [remote-addr server-name session]}]
  (if-let [user {:screenname   "Bob Bobberton"
                 :account-name nil
                 :member-of    nil}
           #_(authenticate userid pass)]
    (let [user (-> user
                   ;; user :screenname as preferred name
                   ;; fall back to userid if not supplied
                   (update-in [:screenname] #(or (not-empty %) userid))
                   (db/update-user-info!)
                   (merge
                     {:client-ip      remote-addr
                      :source-address server-name}))]
      (log/info "user:" userid "successfully logged in from" remote-addr server-name)
      (-> {:user user}
          (ok)
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" userid remote-addr server-name)
      (unauthorized {:error "The username or password was incorrect."}))))

(defn logout []
  (assoc (ok {:result "ok"}) :session nil))
