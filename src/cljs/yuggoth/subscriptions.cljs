(ns yuggoth.subscriptions
  (:require [reagent.ratom :refer [reaction]]
            [re-frame.core :refer [reg-sub-raw]]))

(reg-sub-raw
  :active-page
  (fn [db]
    (reaction (:active-page @db))))

(reg-sub-raw
  :loading?
  (fn [db]
    (reaction (:loading? @db))))

(reg-sub-raw
  :user
  (fn [db]
    (reaction (:user @db))))