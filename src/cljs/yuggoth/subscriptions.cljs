(ns yuggoth.subscriptions
  (:require [reagent.ratom :refer [reaction]]
            [re-frame.core :refer [register-sub]]))

(register-sub
  :active-page
  (fn [db]
    (reaction (:active-page @db))))

(register-sub
  :loading?
  (fn [db]
    (reaction (:loading? @db))))

(register-sub
  :user
  (fn [db]
    (reaction (:user @db))))