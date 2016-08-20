(ns yuggoth.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]
            [yuggoth.bootstrap :as bs]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn home-page []
  (let [tags   (subscribe [:tags])
        issues (subscribe [:issues])]
    [:div.container
     [:div.row
      [:div.col-md-2
       [:h2 "Tags"]
       [:p "todo list tags"]
       [:ul
        (for [{:keys [tag-id tag]} @tags]
          ^{:key tag-id}
          [:ul tag])]]
      [:div.col-md-10
       [:h2 "Recent Issues"]
       [:p "list most recently viewed issues by default"]
       (for [{:keys [support-issue-id title summary tags]} @issues]
         ^{:key support-issue-id}
         [bs/Panel
          {:header (r/as-component [:h2 title])
           :footer (str "tags: " (s/join ", " tags))}
          summary])]]]))