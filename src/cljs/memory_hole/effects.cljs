(ns memory-hole.effects
  (:require [re-frame.core :as rf :refer [dispatch reg-fx]]))

(reg-fx
 :http
 (fn [{:keys [method
              url
              success-event
              error-event
              ajax-map]
       :or {error-event [:ajax-error]
            ajax-map {}}}]
   (dispatch [:set-loading])
   (method url (merge ajax-map
                      {:handler (fn [response]
                                  (dispatch (conj success-event response))
                                  (dispatch [:unset-loading]))
                       :error-handler (fn [error]
                                        (dispatch (conj error-event error))
                                        (dispatch [:unset-loading]))}))))
