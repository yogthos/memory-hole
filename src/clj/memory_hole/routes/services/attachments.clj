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

(handler attach-file-to-issue! [support-issue-id {:keys [tempfile filename content-type]}]
  (if
    (empty? filename)
    (bad-request "a file must be selected")
    (ok
      (db/attach-file-to-issue!
        support-issue-id
        (.replaceAll filename "[^a-zA-Z0-9-_\\.]" "")
        content-type
        (file->byte-array tempfile)))))

(handler delete-file! [name]
  (ok (db/delete-file<! {:name name})))

(defn load-file-data [name]
  (if-let [{:keys [type data]} (db/load-file-data {:name name})]
    (-> (ByteArrayInputStream. data)
        (ok)
        (content-type type))
    (error-page {:status 404
                 :title  "file not found"})))
