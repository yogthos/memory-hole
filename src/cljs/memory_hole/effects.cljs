(ns memory-hole.effects
  (:require [re-frame.core :as rf :refer [dispatch reg-fx reg-event-fx]]
            [accountant.core :as accountant]))

(reg-fx
 :http
 (fn [{:keys [method
              url
              success-event
              error-event
              ignore-response-body
              ajax-map]
       :or {error-event [:ajax-error]
            ajax-map {}}}]
   (dispatch [:set-loading])
   (method url (merge ajax-map
                      {:handler (fn [response]
                                  (dispatch (if ignore-response-body
                                              success-event
                                              (conj success-event response)))
                                  (dispatch [:unset-loading]))
                       :error-handler (fn [error]
                                        (dispatch (conj error-event error))
                                        (dispatch [:unset-loading]))}))))
(defn context-url [url]
  (str js/context url))

(reg-fx
 :navigate
 (fn [url]
   (accountant/navigate! (context-url url))))
