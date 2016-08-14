(ns yuggoth.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [yuggoth.routes.services.auth :as auth]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
    :tags ["auth"]

    (POST "/login" req
      :return auth/LoginResponse
      :body-params [userid :- String, pass :- String]
      :summary "User login handler"
      (auth/login userid pass req))

    (POST "/logout" []
      :return auth/LogoutResponse
      :summary "remove the user from the session"
      (auth/logout))





    (GET "/plus" []
      :return       Long
      :query-params [x :- Long, {y :- Long 1}]
      :summary      "x+y with query-parameters. y defaults to 1."
      (ok (+ x y)))

    (POST "/minus" []
      :return      Long
      :body-params [x :- Long, y :- Long]
      :summary     "x-y with body-parameters."
      (ok (- x y)))

    (GET "/times/:x/:y" []
      :return      Long
      :path-params [x :- Long, y :- Long]
      :summary     "x*y with path-parameters"
      (ok (* x y)))

    (POST "/divide" []
      :return      Double
      :form-params [x :- Long, y :- Long]
      :summary     "x/y with form-parameters"
      (ok (/ x y)))

    (GET "/power" []
      :return      Long
      :header-params [x :- Long, y :- Long]
      :summary     "x^y with header-parameters"
      (ok (long (Math/pow x y))))))
