(ns memory-hole.routes.services.auth
  (:require [memory-hole.config :refer [env]]
            [memory-hole.db.core :as db]
            [memory-hole.validation :as v]
            [memory-hole.routes.services.common :refer [handler]]
            [buddy.hashers :as hashers]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]
            [clj-ldap.client :as client]
            [schema.core :as s]
            [clojure.set :refer [rename-keys]]
            [ring.util.http-response :refer :all]))

(defstate host :start (:ldap env))
(defstate ldap-pool :start (when host (client/connect host)))

(defn authenticate-ldap [userid pass]
  (let [conn           (client/get-connection ldap-pool)
        qualified-name (str userid "@" (-> host :host :domain))]
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
               :sAMAccountName :account-name})
            (update :member-of #(if (string? %) [%] %))))
      (finally (client/release-connection ldap-pool conn)))))

(defn authenticate-local [userid pass]
  (when-let [user (db/user-by-screenname {:screenname userid})]
    (when (hashers/check pass (:pass user))
      (dissoc user :pass))))

(def User
  {:user-id                         s/Int
   :screenname                      (s/maybe s/Str)
   :admin                           s/Bool
   :is-active                       s/Bool
   :last-login                      java.util.Date
   (s/optional-key :member-of)      [(s/maybe s/Str)]
   (s/optional-key :account-name)   (s/maybe s/Str)
   (s/optional-key :client-ip)      s/Str
   (s/optional-key :source-address) s/Str})

(def SearchResponse
  {(s/optional-key :users) [User]
   (s/optional-key :error) s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(handler find-users [screenname]
  (ok
    {:users
     (db/users-by-screenname
       {:screenname (str "%" screenname "%")})}))

(handler register! [user]
  (if-let [errors (v/validate-create-user user)]
    (do
      (log/error "error creating user:" errors)
      (bad-request {:error "invalid user"}))
    (db/insert-user<!
      (-> user
          (dissoc :pass-confirm)
          (update-in [:pass] hashers/encrypt)))))

(handler update-user! [{:keys [pass] :as user}]
  (if-let [errors (v/validate-update-user user)]
    (do
      (log/error "error updating user:" errors)
      (bad-request {:error "invalid user"}))
    (ok
      {:user
       (if pass
         (db/update-user-with-pass<!
           (-> user
               (dissoc :pass-confirm)
               (update :pass hashers/encrypt)))
         (db/update-user<! user))})))

(defn local-login [userid pass]
  (when-let [user (authenticate-local userid pass)]
    (-> user
        (merge {:member-of    []
                :account-name userid}))))

(defn ldap-login [userid pass]
  (when-let [user (authenticate-ldap userid pass)]
    (-> user
        ;; user :screenname as preferred name
        ;; fall back to userid if not supplied
        (assoc :admin false
               :is-active true)
        (update-in [:screenname] #(or (not-empty %) userid))
        (db/update-user-info!))))

(defn login [userid pass {:keys [remote-addr server-name session]}]
  (if-let [user (if (:ldap env)
                  (ldap-login userid pass)
                  (local-login userid pass))]
    (let [user (-> user
                   (dissoc :pass)
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

(handler logout []
  (assoc (ok {:result "ok"}) :session nil))
