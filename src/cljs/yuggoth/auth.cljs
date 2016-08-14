(ns yuggoth.auth
  (:require [yuggoth.bootstrap :as bs]
            [reagent.core :as r]))

#_(defn login-form
  []
  (let [values    (r/atom nil)
        error     (r/atom nil)
        on-close  (fn []
                    (reset! values nil)
                    (reset! error nil)
                    (auth/hide-login-form!))
        on-submit (fn []
                    (reset! error nil)
                    (let [{:keys [username password]} @values]
                      (if (or (string/blank? username) (string/blank? password))
                        (reset! error "Username and password cannot be blank.")
                        (ajax/POST (->url "/login")
                                   :params {:username username :password password}
                                   :on-error #(reset! error "Invalid username/password.")
                                   :on-success (fn [response]
                                                 (let [user-profile (clojure.walk/keywordize-keys response)]
                                                   (on-close)
                                                   (views/reconnect!)
                                                   (auth/set-user-profile! user-profile)))))))
        on-key-up (fn [e]
                    (if (= 13 (.-keyCode e))
                      (on-submit)))]
    (fn []
      [bs/Modal
       {:show    (boolean @auth/show-login)
        :on-hide on-close}
       [bs/Modal.Header [bs/Modal.Title "Login"]]
       [bs/Modal.Body
        (if @error
          [bs/Alert {:bsStyle "danger"} @error])
        [bs/Form {:horizontal true}
         [bs/FormGroup
          [bs/Col {:class "text-right" :sm 4} [bs/ControlLabel "Username"]]
          [bs/Col {:sm 6}
           [bs/FormControl
            {:type      "text"
             :value     (or (:username @values) "")
             :on-change #(swap! values assoc :username (get-field-value %))
             :on-key-up on-key-up}]]]
         [bs/FormGroup
          [bs/Col {:class "text-right" :sm 4} [bs/ControlLabel "Password"]]
          [bs/Col {:sm 6}
           [bs/FormControl
            {:type      "password"
             :value     (or (:password @values) "")
             :on-change #(swap! values assoc :password (get-field-value %))
             :on-key-up on-key-up}]]]]]
       [bs/Modal.Footer
        [bs/Button {:bsStyle "primary" :on-click on-submit} "Login"]
        [bs/Button {:on-click on-close} "Cancel"]]])))