(ns memory-hole.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [memory-hole.routes :refer [set-location!]]
            [memory-hole.pages.issues :refer [markdown-component]]
            [memory-hole.key-events :refer [on-enter]]
            [memory-hole.bootstrap :as bs]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn issue-search [select]
  (r/with-let [search    (r/atom nil)
               do-search #(when-let [value (not-empty @search)]
                           (select [:search-for-issues value] nil))]
    [bs/FormGroup
     [bs/InputGroup
      [bs/FormControl
       {:type        "text"
        :placeholder "type in issue details to find matching issues"
        :on-change   #(reset! search (-> % .-target .-value))
        :on-key-down #(on-enter % do-search)}]
      [bs/InputGroup.Button
       [bs/Button
        {:on-click do-search}
        "search"]]]]))

(defn new-issue []
  [:span.pull-right
   [bs/Button {:bs-style "primary"
               :on-click #(set-location! "#/create-issue")}
    "Add Issue"]])

(defn issue-panel [{:keys [support-issue-id title summary views]}]
  [:div.panel.panel-default
   [:div.panel-heading.issue-title
    [:h3>a {:href (str "#/issue/" support-issue-id)}
     title [:span.pull-right [bs/Badge views]]]]
   [:div.panel-body summary]])

(defn tag-control [title count selected on-click]
  [bs/ListGroupItem
   {:on-click on-click
    :active   (= title @selected)}
   [:b title] " "
   (when count [bs/Badge count])])

(defn tags-with-issues [tags]
  (->> tags
       (filter #(pos? (:tag-count %)))
       (sort-by :tag-count)
       (reverse)))

(defn home-page []
  (r/with-let [tags       (subscribe [:tags])
               issues     (subscribe [:issues])
               selected   (subscribe [:selected-tag])
               select     (fn [action selection]
                            (set-location! "#/")
                            (.scrollTo js/window 0 0)
                            (dispatch action)
                            (dispatch [:select-tag selection]))
               select-tag (fn [selection]
                            (.scrollTo js/window 0 0)
                            (set-location! "#/issues/" selection))]
    [:div.container
     [:div.row
      [:div.col-md-3
       [:h3 "Tags"]
       [bs/ListGroup
        [tag-control
         "All"
         nil
         selected
         #(select [:load-all-issues] "All")]
        [tag-control
         "Recent"
         nil
         selected
         #(select [:load-recent-issues] "Recent")]
        [tag-control
         "Most Viewed"
         nil
         selected
         #(select [:load-most-viewed-issues] "Most Viewed")]
        (for [{:keys [tag-count tag-id tag]} (tags-with-issues @tags)]
          ^{:key tag-id}
          [tag-control
           tag
           tag-count
           selected
           #(select-tag tag)])]]
      [:div.col-md-9
       [:h3 "Issues "
        (when-let [tag @selected]
          [bs/Badge tag])
        [new-issue]]
       [issue-search select]
       (for [issue-summary @issues]
         ^{:key (:support-issue-id issue-summary)}
         [issue-panel issue-summary])]]]))