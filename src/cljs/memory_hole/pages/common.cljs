(ns memory-hole.pages.common
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [memory-hole.bootstrap :as bs]))

(def spacer [:span {:style {:margin-right "5px"}}])

(defn loading-throbber
  []
  (let [loading? (subscribe [:loading?])]
    (when @loading?
      [bs/Modal
       {:show true}
       [bs/Modal.Body
        [:div.spinner
         [:div.bounce1]
         [:div.bounce2]
         [:div.bounce3]]]])))

(defn error-modal []
  (when-let [error @(subscribe [:error])]
    [bs/Modal {:show error}
     [bs/Modal.Header
      [bs/Modal.Title "and error has occured"]]
     [bs/Modal.Body
      [:p error]
      [bs/Button
       {:bs-style "danger"
        :on-click #(dispatch [:set-error] nil)}
       "OK"]]]))

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
