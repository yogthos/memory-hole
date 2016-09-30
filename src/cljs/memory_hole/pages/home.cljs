(ns memory-hole.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [memory-hole.pages.issues :refer [markdown-component]]
            [memory-hole.key-events :refer [on-enter]]
            [memory-hole.bootstrap :as bs]
            [memory-hole.routes :refer [href navigate!]]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]))

(defn issue-search []
  (r/with-let [search    (r/atom nil)
               do-search #(when-let [value (not-empty @search)]
                           (navigate! (str "/search/" value)))]
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
   [:a.btn.btn-primary
    (href "/create-issue") "Add Issue"]])

(defn issue-panel [{:keys [support-issue-id title summary views]}]
  [:div.panel.panel-default
   [:div.panel-heading.issue-title
    [:h3>a (href (str "/issue/" support-issue-id))
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
               selected   (subscribe [:selected-tag])]
    [:div.container
     [:div.row
      [:div.col-md-3
       [:h3 "Tags"]
       [bs/ListGroup
        [tag-control
         "All"
         nil
         selected
         #(navigate! "/all-issues")]
        [tag-control
         "Recent"
         nil
         selected
         #(navigate! "/recent-issues")]
        [tag-control
         "Most Viewed"
         nil
         selected
         #(navigate! "/most-viewed-issues")]
        (for [{:keys [tag-count tag-id tag]} (tags-with-issues @tags)]
          ^{:key tag-id}
          [tag-control
           tag
           tag-count
           selected
           #(navigate! (str "/issues/" tag))])]]
      [:div.col-md-9
       [:h3 "Issues "
        (when-let [tag @selected]
          [bs/Badge tag])
        [new-issue]]
       [issue-search]
       (for [issue-summary @issues]
         ^{:key (:support-issue-id issue-summary)}
         [issue-panel issue-summary])]]]))
