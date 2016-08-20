(ns yuggoth.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :active-page
  (fn [db _]
    (:active-page db)))

(reg-sub
  :loading?
  (fn [db _]
    (:loading? db)))

(reg-sub
  :user
  (fn [db _]
    (:user db)))

(reg-sub
  :tags
  (fn [db _]
    (:tags db)))

(reg-sub
  :issue
  (fn [db _]
    (:issue db)))

(reg-sub
  :issues
  (fn [db _]
    (:issues db)))

(reg-sub
  :error
  (fn [db _]
    (:error db)))

(reg-sub
  :login-events
  (fn [db _]
    (:login-events db)))

(reg-sub
  :db-state
  (fn [db _]
    (:issue db)))