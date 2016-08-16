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

(reg-sub-raw
  :tags
  (fn [db]
    (reaction (:tags @db))))

(reg-sub-raw
  :issue
  (fn [db]
    (reaction (:issue @db))))

(reg-sub-raw
  :issues
  (fn [db]
    (reaction (:issues @db))))

(reg-sub-raw
  :error
  (fn [db]
    (reaction (:error @db))))

(reg-sub-raw
  :db-state
  (fn [db]
    (reaction (:issue @db))))