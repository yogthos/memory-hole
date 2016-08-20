(ns yuggoth.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as s]
            [yuggoth.pages.issues :refer [markdown-component]]
            [yuggoth.bootstrap :as bs]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn issue-search [select]
  (r/with-let [search (r/atom nil)]
    [bs/FormGroup
     [bs/InputGroup
      [bs/FormControl
       {:type      "text"
        :on-change #(reset! search (-> % .-target .-value))}]
      [bs/InputGroup.Button
       [bs/Button
        {:on-click #(when-let [value (not-empty @search)]
                     (select [:search-for-issues value] nil))}
        "search"]]]]))

(defn new-issue []
  [:button.btn.btn-primary.pull-right
   {:on-click #(dispatch [:set-active-page :edit-issue])}
   "Add Issue"])

(defn issue-panel [{:keys [support-issue-id title summary]}]
  [bs/Panel
   {:header (r/as-component [:h2 title])
    :footer (r/as-component
              [:a {:href (str "#/issue/" support-issue-id)}
               "more..."])}
   summary])

(defn tag-control [title selected on-click]
  [bs/ListGroupItem
   {:on-click on-click}
   title
   (when (= title @selected)
     [:span.glyphicon.glyphicon-triangle-right])])

(defn home-page []
  (r/with-let [tags     (subscribe [:tags])
               issues   (subscribe [:issues])
               selected (r/atom nil)
               select   (fn [action selection]
                          (dispatch action)
                          (reset! selected selection))]
    [:div.container
     [:div.row
      [:div.col-md-2
       [:h2 "Tags"]
       [bs/ListGroup
        [tag-control
         "Recent"
         selected
         #(select [:load-recent-issues] "Recent")]
        (doall
          (for [{:keys [tag-id tag]} @tags]
            ^{:key tag-id}
            [tag-control
             tag
             selected
             #(select [:load-issues-for-tag tag] tag)]))]]
      [:div.col-md-10
       [:h2 "Issues " [new-issue]]
       [issue-search select]
       (for [issue-summary @issues]
         ^{:key (:support-issue-id issue-summary)}
         [issue-panel issue-summary])]]]))