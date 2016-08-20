(ns yuggoth.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as s]
            [yuggoth.bootstrap :as bs]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn issue-search []
  (r/with-let [search (r/atom nil)]
    [bs/FormGroup
     [bs/InputGroup
      [bs/FormControl
       {:type      "text"
        :on-change #(reset! search (-> % .-target .-value))}]
      [bs/InputGroup.Button
       [bs/Button
        {:on-click #(when-let [value (not-empty @search)]
                     (dispatch [:search-for-issues value]))}
        "search"]]]]))

(defn home-page []
  (r/with-let [tags         (subscribe [:tags])
               issues       (subscribe [:issues])]
    [:div.container
     [:div.row
      [:div.col-md-2
       [:h2 "Tags"]
       [bs/ListGroup
        [bs/ListGroupItem {:on-click #(dispatch [:load-recent-issues])}
         "Recent"]
        (for [{:keys [tag-id tag]} @tags]
         ^{:key tag-id}
         [bs/ListGroupItem
          {:on-click #(dispatch [:load-issues-for-tag tag])}
          tag])]]
      [:div.col-md-10
       [:h2 "Issues"]
       [issue-search]
       (for [{:keys [support-issue-id title summary]} @issues]
         ^{:key support-issue-id}
         [bs/Panel
          {:header (r/as-component [:h2 title])}
          summary])]]]))