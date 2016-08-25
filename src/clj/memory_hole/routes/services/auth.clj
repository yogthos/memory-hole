(ns memory-hole.routes.services.auth
  (:require [memory-hole.config :refer [env]]
            [memory-hole.db.core :as db]
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

(defn authenticate-local [userid pass]
  (when-let [user (db/user-by-screenname {:screenname userid})]
    (when (hashers/check pass (:pass user))
      (dissoc user :pass))))

(def User
  {:user-id        s/Int
   :member-of      [(s/maybe s/Str)]
   :screenname     (s/maybe s/Str)
   :account-name   (s/maybe s/Str)
   :last-login     java.util.Date
   :admin          s/Bool
   :is-active      s/Bool
   :client-ip      s/Str
   :source-address s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(handler register! [{:keys [pass pass1] :as user} admin?]
  (cond
    (not admin?)
    (unauthorized {:error "you do not have permission to add users"})
    (= pass pass1)
    (db/insert-user<!
      (-> user
          (update-in [:pass] hashers/encrypt)
          (dissoc :pass1)))
    :else
    (bad-request {:error "invalid user"})))

(handler update-user! [user admin?]
  (if admin?
    (ok {:user (db/update-user<! user)})
    (unauthorized {:error "you do not have permission to edit users"})))

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

(defn logout []
  (assoc (ok {:result "ok"}) :session nil))
