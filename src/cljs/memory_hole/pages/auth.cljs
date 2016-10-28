(ns memory-hole.pages.auth
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [ajax.core :refer [POST]]
            [memory-hole.bootstrap :as bs]
            [re-frame.core :refer [dispatch subscribe]]))

(defn logged-in? []
  (boolean js/user))

(defn logout []
  (POST "/api/logout"
        {:handler (dispatch [:logout])}))

(defn login [params error on-close]
  (reset! error nil)
  (let [{:keys [userid pass]} @params]
    (if (or (string/blank? userid) (string/blank? pass))
      (reset! error "Username and password cannot be blank.")
      (POST "/api/login"
            {:params        {:userid userid :pass pass}
             :error-handler #(reset! error "Invalid username/password.")
             :handler       #(let [user (:user %)]
                              (on-close)
                              (dispatch [:login user])
                              (dispatch [:set-active-page :home]))}))))

(defn login-page []
  (r/with-let [user      (subscribe [:user])
               params    (r/atom nil)
               error     (r/atom nil)
               on-close  #(do
                           (reset! params nil)
                           (reset! error nil))
               on-key-up (fn [e]
                           (if (= 13 (.-keyCode e))
                             (login params error on-close)))]
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
        [:button.btn.btn-sm.btn-primary {:on-click #(login params error on-close)} "Login"]]])))
