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