(ns memory-hole.pages.common
  (:require [memory-hole.bootstrap :as bs]))

(def spacer [:span {:style {:margin-right "5px"}}])

(defn validation-modal [errors]
  [bs/Modal {:show (boolean @errors)}
   [bs/Modal.Header
    [bs/Modal.Title "Missing required fields"]]
   [bs/Modal.Body
    [:ul
     (for [[_ error] @errors]
       ^{:key error}
       [:li error])]
    [bs/Button {:bs-style "danger"
                :on-click #(reset! errors nil)}
     "Close"]]])

(defn confirm-modal [title confirm-open? action action-label]
  [bs/Modal {:show @confirm-open?}
   [bs/Modal.Header
    [bs/Modal.Title title]]
   [bs/Modal.Body
    [bs/Button {:bs-style "danger"
                :on-click #(reset! confirm-open? false)}
     "Cancel"]
    spacer
    [bs/Button {:bs-style   "primary"
                :pull-right true
                :on-click   #(do
                              (reset! confirm-open? false)
                              (action))}
     action-label]]])
