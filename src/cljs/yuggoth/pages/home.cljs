(ns yuggoth.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [yuggoth.bootstrap :as bs]))

(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "TrackIt"]]]])