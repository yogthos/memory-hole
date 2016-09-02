(ns memory-hole.ajax
  (:require [re-frame.core :refer [dispatch]]
            [ajax.core :as ajax]))

(defn ajax-error [response]
  (if (= 401 (:status response))
    (dispatch [:logout])
    (dispatch [:set-error (-> response :response :error)])))

(defn request-defaults [request]
  (dispatch [:set-loading])
  (-> request
      (update :uri #(str js/context %))
      (update
        :headers
        #(merge
          %
          {"Accept" "application/transit+json"
           "x-csrf-token" js/csrfToken}))))

(defn response-defaults [response]
  (dispatch [:unset-loading])
  response)

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name     "default headers"
                               :request  request-defaults
                               :response response-defaults})))


