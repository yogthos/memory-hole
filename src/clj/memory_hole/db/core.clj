(ns memory-hole.db.core
  (:require
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.java.jdbc :as jdbc]
   [clojure.set :refer [difference]]
   [clojure.walk :refer [postwalk]]
   [conman.core :as conman]
   [cuerdas.core :as string]
   [mount.core :refer [defstate]]
   [memory-hole.config :refer [env]]
   [buddy.hashers :as hashers]
   [clojure.tools.logging :as log])
  (:import org.postgresql.util.PGobject
           org.h2.jdbc.JdbcClob
           java.sql.Array
           java.util.LinkedList
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            Date
            Timestamp
            PreparedStatement]))

(defstate ^:dynamic *db-type*
  :start (->> (:database-url env)
              (re-find #"jdbc:([^.]+?):.+")
              second
              keyword)
  :stop nil)

(defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db*
                        "sql/issues.sql"
                        "sql/tags.sql"
                        "sql/users.sql"
                        "sql/groups.sql"
                        "sql/attachments.sql")

(defn ->kebab-case-keyword* [k]
  (-> (reduce
        (fn [s c]
          (if (and
                (not-empty s)
                (Character/isLowerCase (last s))
                (Character/isUpperCase c))
            (str s "-" c)
            (str s c)))
        "" (name k))
      (string/replace #"[\s]+" "-")
      (.replaceAll "_" "-")
      (.toLowerCase)
      (keyword)))

(def ->kebab-case-keyword (memoize ->kebab-case-keyword*))

(defn transform-keys [t coll]
  "Recursively transforms all map keys in coll with t."
  (letfn [(transform [[k v]] [(t k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))

(defn unified-map-returning [x]
  (if (map? x) x (first x)))

(defn unified-handling-single [this result options]
  (case (:command options)
    :i!                (unified-map-returning result)
    :insert            (unified-map-returning result)
    :<!                (hugsql.adapter/result-one this result options)
    :returning-execute (hugsql.adapter/result-one this result options)
    :!                 (hugsql.adapter/result-one this result options)
    :execute           (hugsql.adapter/result-one this result options)
    :?                 (hugsql.adapter/result-one this result options)
    :query             (hugsql.adapter/result-one this result options)))

(defn result-one-snake->kebab
  [this result options]
  (->> (unified-handling-single this result options)
       (transform-keys ->kebab-case-keyword*)))

(defn result-many-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (map #(transform-keys ->kebab-case-keyword %))))

(defmethod hugsql.core/hugsql-result-fn :1 [sym]
  'memory-hole.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :* [sym]
  'memory-hole.db.core/result-many-snake->kebab)

(defn deserialize [pgobj]
  (let [type  (.getType pgobj)
        value (.getValue pgobj)]
    (case type
      "record" (some-> value (subs 1 (dec (count value))) (.split ",") vec)
      "json" (parse-string value true)
      "jsonb" (parse-string value true)
      "citext" (str value)
      value)))

(defn to-date [^java.sql.Date sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(defn- db->clj [x]
  "Converts common types returned from DB to Clojure data."
  (condp = (type x)
    java.io.BufferedReader (.readLine x)
    x))

(extend-protocol jdbc/IResultSetReadColumn
  (Class/forName "[Ljava.lang.Object;")
  (result-set-read-column [v _ _]
    (vec (map db->clj v)))

  Date
  (result-set-read-column [v _ _] (to-date v))

  Timestamp
  (result-set-read-column [v _ _] (to-date v))

  Array
  (result-set-read-column [v _ _]
    (->> (.getArray v)
         (map #(if (instance? PGobject %)
                (deserialize %) %))
         (remove nil?)
         (vec)))

  LinkedList
  (result-set-read-column [v _ _]
    (vec v))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (deserialize pgobj))

  JdbcClob
  (result-set-read-column [h2obj _metadata _index]
    (.getSubString h2obj 1 (.length h2obj))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt ^long idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

(defn- retrieve-id [x]
  (case *db-type*
    :postgresql x
    :h2         ((keyword "scope-identity()") x)))

(defn with-returning
  "Mimicks RETURNING statement by mapping id-name to .getReturnedKeys and merging it to m."
  [f m id-name]
  (let [id-keyword (keyword id-name)
        result (f m)]
    (if (contains? result id-keyword)
      result
      (->> {id-keyword (retrieve-id result)}
           (merge m)))))

(defn support-issue [m]
  (conman/with-transaction [*db*]
    (when-let [issue (not-empty (support-issue* m))]
      (-> issue
          (update :tags distinct)
          (update :files distinct)
          (merge {:views (inc-issue-views! m)})
          (merge (get-views-count m))))))

(defn create-missing-tags [issue-tags]
  (let [current-tags (map :tag (tags))]
    (doseq [tag (difference (set issue-tags)
                            (set current-tags))]
      (with-returning create-tag<! {:tag tag} :tag-id))))

(defn reset-issue-tags! [user-id support-issue-id tags]
  (create-missing-tags tags)
  (dissoc-tags-from-issue!
   {:support-issue-id support-issue-id
    :user-id user-id})
  (assoc-tags-with-issue!
   {:support-issue-id support-issue-id
    :tags             tags
    :user-id user-id}))

(defn user-can-access-group [{:keys [user-id group-id]}]
  (some
   (comp (partial = group-id) :group-id)
   (groups-for-user {:user-id user-id})))

(defn create-issue-with-tags! [{:keys [tags user-id] :as issue}]
  (conman/with-transaction [*db*]
    (when (user-can-access-group (select-keys issue [:user-id :group-id]))
      (let [support-issue-id (:support-issue-id
                              (with-returning add-issue<! (dissoc issue :tags) :support-issue-id))]
        (reset-issue-tags! user-id support-issue-id tags)
        support-issue-id))))

(defn update-issue-with-tags! [{:keys [user-id support-issue-id tags] :as issue}]
  (conman/with-transaction [*db*]
    (when (user-can-access-group (select-keys issue [:user-id :group-id]))
      (reset-issue-tags! user-id support-issue-id tags)
      (update-issue! (dissoc issue :tags)))))

(defn dissoc-from-tags-and-delete-issue-and-files! [m]
  (conman/with-transaction [*db*]
    (delete-issue-files! m)
    (dissoc-tags-from-issue! m)
    (delete-issue! m)))

(defn insert-user-with-belongs-to!
  "inserts a user and adds them to specified groups in a transaction."
  [{:keys [screenname pass admin is-active belongs-to] :as user}]
  (conman/with-transaction [*db*]
    (let [{:keys [user-id]}
          (with-returning
            insert-user<!
            {:screenname screenname
             :admin admin
             :is-active is-active
             :db-type *db-type*
             :pass pass}
            :user-id)]
      (add-user-to-groups! {:user-id user-id
                            :groups belongs-to})
      (user-by-screenname {:screenname screenname}))))

(defn update-or-insert-user-with-belongs-to!
  "updates a user and modifies their group membership."
  [{:keys [screenname pass admin is-active belongs-to member-of update-password?] :as user}]
  (conman/with-transaction [*db*]
    (let [belongs-to (into [] (distinct (concat belongs-to member-of)))
          existing-user (user-by-screenname {:screenname screenname})
          user-exists? (not (empty? existing-user))
          {:keys [user-id]} (if user-exists?
                              existing-user
                              (insert-user<! {:screenname screenname
                                              :admin admin
                                              :is-active is-active
                                              :pass pass}))
          old-groups (:belongs-to existing-user [])
          del-groups (remove (set belongs-to) old-groups)
          add-groups (remove (set old-groups) belongs-to)]
      (when user-exists?
        (if update-password?
          (update-user-with-pass<! (-> user
                                       (select-keys [:screenname
                                                     :pass
                                                     :admin
                                                     :is-active
                                                     :user-id])
                                       (merge {:db-type *db-type*})))
          (update-user<! {:user-id    user-id
                          :admin      admin
                          :is-active  is-active
                          :screenname screenname
                          :db-type *db-type*})))
      (when-not (empty? del-groups)
        (remove-user-from-groups! {:user-id user-id
                                   :groups del-groups}))
      (when-not (empty? add-groups)
        (add-user-to-groups! {:user-id user-id
                              :groups add-groups}))
      (select-keys
       (user-by-screenname {:screenname screenname})
       [:user-id
        :screenname
        :admin
        :is-active
        :last-login
        :belongs-to]))))

(defn run-query-if-user-can-access-issue
  "runs query-fn with no args if user can access, otherwise returns nil"
  [{:keys [user-id support-issue-id] :as m} query-fn]
  (conman/with-transaction [*db*]
    (when (user-can-access-issue? m)
      (query-fn))))
