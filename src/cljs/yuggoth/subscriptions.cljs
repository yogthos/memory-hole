(ns yuggoth.subscriptions
  (:require [reagent.ratom :refer [reaction]]
            [re-frame.core :refer [register-sub]]))

(register-sub
  :active-page
  (fn [db _]
    (reaction (:active-page @db))))

(register-sub
  :loading?
  (fn [db]
    (reaction (:loading? @db))))