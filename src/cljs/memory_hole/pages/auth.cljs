(ns memory-hole.pages.auth
  (:require [reagent.core :as r]
            [memory-hole.bootstrap :as bs]
            [re-frame.core :refer [dispatch subscribe]]))

(defn login [params on-close]
  (let [{:keys [userid pass]} @params]
    (dispatch [:login userid pass])))

(defn login-page []
  (r/with-let [user      (subscribe [:user])
               params    (r/atom nil)
               error     (subscribe [:error])
               on-close  (fn []
                           (reset! params nil)
                           (dispatch [:clear-error]))
               on-key-up (fn [e]
                           (if (= 13 (.-keyCode e))
                             (login params on-close)))]
    (when-not @user
      [bs/Modal
       {:show    true
        :on-hide on-close}
       [bs/Modal.Header [bs/Modal.Title "Login"]]
       [bs/Modal.Body
        (if @error
          [bs/Alert {:bs-style "danger"} @error])
        [bs/Form {:horizontal true}
         [bs/FormGroup
          [bs/Col {:class "text-right" :sm 4} [bs/ControlLabel "Username"]]
          [bs/Col {:sm 6}
           [:input.form-control
            {:type      "text"
             :value     (or (:userid @params) "")
             :on-change #(swap! params assoc :userid (-> % .-target .-value))
             :on-key-up on-key-up}]]]
         [bs/FormGroup
          [bs/Col {:class "text-right" :sm 4} [bs/ControlLabel "Password"]]
          [bs/Col {:sm 6}
           [:input.form-control
            {:type      "password"
             :value     (or (:pass @params) "")
             :on-change #(swap! params assoc :pass (-> % .-target .-value))
             :on-key-up on-key-up}]]]]]
       [bs/Modal.Footer
        [:button.btn.btn-sm.btn-primary {:on-click #(login params on-close)} "Login"]]])))
