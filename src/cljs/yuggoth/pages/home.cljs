(ns yuggoth.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [yuggoth.bootstrap :as bs]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-2
     {:style {:border "solid"}}
     [:h2 "Tags"]]
    [:div.col-md-10
     [:h2 "TrackIt"]]]])