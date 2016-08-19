(ns yuggoth.routes.services.auth
  (:require [yuggoth.config :refer [env]]
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
              {:displayName    :display-name
               :memberOf       :member-of
               :sAMAccountName :account-name})))
      (finally (client/release-connection ldap-pool conn)))))

(def User
  {:id             s/Str
   :member-of      [(s/maybe s/Str)]
   :display-name   (s/maybe s/Str)
   :account-name   (s/maybe s/Str)
   :client-ip      s/Str
   :source-address s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(defn login [userid pass {:keys [remote-addr server-name session]}]
  (println "LOGIN!!!!")
  (if-let [user {:id           1
                 :display-name "Bob Bobberton"
                 :account-name nil
                 :member-of    nil}
           #_(authenticate userid pass)]
    (do
      (log/info "user:" userid "successfully logged in from" remote-addr server-name)
      (->
        {:user
         (-> user
             ;;user :display-name as preferred name, fall back to userid if not supplied
             (update-in [:display-name] #(or (not-empty %) userid))
             (merge
               {:id             userid
                :client-ip      remote-addr
                :source-address server-name}))}
        ok
        (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" userid remote-addr server-name)
      (unauthorized {:error "The username or password was incorrect."}))))

(defn logout []
  (assoc (ok {:result "ok"}) :session nil))