(ns memory-hole.routes.services.attachments
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :refer :all]
            [memory-hole.layout :refer [error-page]]
            [memory-hole.db.core :as db]
            [memory-hole.routes.services.common :refer [handler]]
            [schema.core :as s])
  (:import [java.io ByteArrayOutputStream
                    ByteArrayInputStream
                    FileInputStream]))

(def AttachmentResult
  {(s/optional-key :name)  s/Str
   (s/optional-key :error) s/Str})

(defn file->byte-array [x]
  (with-open [input  (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

(handler attach-file-to-issue! [{:keys [support-issue-id user-id] :as m} {:keys [tempfile filename content-type]}]
  (if (empty? filename)
    (bad-request "a file must be selected")
    (let [db-file-name (.replaceAll filename "[^a-zA-Z0-9-_\\.]" "")]
      (if (db/run-query-if-user-can-access-issue
           (select-keys m [:user-id :support-issue-id])
           #(db/save-file! {:support-issue-id support-issue-id
                            :type             content-type
                            :name             db-file-name
                            :data             (file->byte-array tempfile)}))
        (ok {:name db-file-name})
        (bad-request {:error (str "Issue not found for: " (select-keys m [:user-id :support-issue-id]))})))))

(handler remove-file-from-issue! [opts]
  (if-some [result (db/run-query-if-user-can-access-issue
                    (select-keys opts [:user-id :support-issue-id])
                    (fn []
                      (let [local-opts (dissoc opts :user-id)]
                        (db/delete-file<! local-opts)
                        (select-keys local-opts [:name]))))]
    (ok result)
    (bad-request {:error (str "Issue not found for: " (select-keys opts [:user-id :support-issue-id]))})))

(handler load-file-data [file]
  (if-let [{:keys [type data]} (db/run-query-if-user-can-access-issue
                                (select-keys file [:user-id :support-issue-id])
                                #(db/load-file-data (dissoc file :user-id)))]
    (-> (ByteArrayInputStream. data)
        (ok)
        (content-type type))
    (error-page {:status 404
                 :title  "file not found"})))
