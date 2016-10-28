(ns memory-hole.pages.home
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [memory-hole.pages.issues :refer [markdown-preview]]
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
        :class       "input-sm"
        :placeholder "Type in issue details to find matching issues"
        :on-change   #(reset! search (-> % .-target .-value))
        :on-key-down #(on-enter % do-search)}]
      [bs/InputGroup.Button
       [:button.btn.btn-sm.btn-default
        {:on-click do-search}
        "Search"]]]]))

(defn new-issue []
  [:a.btn.btn-sm.btn-success.pull-right
   (href "/create-issue") "Add Issue"])

(defn issue-panel [{:keys [support-issue-id title summary views]}]
  [:div.panel.panel-default
   [:div.panel-heading.issue-title
    [:h3>a (href (str "/issue/" support-issue-id))
     title
     [:span.pull-right [bs/Badge views]]]]
   [:div.panel-body summary]])

(defn sorted-tags [tags sort-type]
  (let [tags (filter #(pos? (:tag-count %)) tags)]
    (case sort-type
      :name (sort-by :tag tags)

      :count (->> tags
                  (sort-by :tag-count)
                  (reverse)))))

(defn tag-control [title count selected on-click]
  [bs/ListGroupItem
   {:on-click on-click
    :active   (= title selected)}
   [:b title] " "
   (when count [bs/Badge count])])

(defn tags-panel [tags selected]
  (r/with-let [sort-type (r/atom :count)]
    [:div
     [:h2 "Tags"]
     [:ul.nav.nav-tabs
      [:li {:class (when (= @sort-type :count) "active")}
       [:a {:on-click #(reset! sort-type :count)}
        "# Issues"]]
      [:li {:class (when (= @sort-type :name) "active")}
       [:a {:on-click #(reset! sort-type :name)}
        "A-Z"]]]
     [:div.panel
      [bs/ListGroup
       (for [{:keys [tag-id tag tag-count]} (sorted-tags tags @sort-type)]
         ^{:key tag-id}
         [tag-control tag tag-count selected #(navigate! (str "/issues/" tag))])]]]))

(defn filter-control [title selected on-click]
  [:button.btn.btn-xs
   {:on-click on-click
    :class    (if (= title selected) "btn-success" "btn-default")}
   title])

(defn filters [selected]
  [:div.btn-toolbar
   [filter-control
    "All"
    selected
    #(navigate! "/all-issues")]
   [filter-control
    "Recent"
    selected
    #(navigate! "/recent-issues")]
   [filter-control
    "Most Viewed"
    selected
    #(navigate! "/most-viewed-issues")]
   (when-not (contains? #{"All" "Recent" "Most Viewed"} selected)
     [:button.btn.btn-xs.btn-success selected])])

(defn home-page []
  (r/with-let [tags     (subscribe [:tags])
               issues   (subscribe [:issues])
               selected (subscribe [:selected-tag])]
    [:div.container
     [:div.row
      [:div.col-sm-3
       [tags-panel @tags @selected]]
      [:div.col-sm-9
       [:h2 "Issues "
        [filters @selected]
        [new-issue]]
       [issue-search]
       (for [issue-summary @issues]
         ^{:key (:support-issue-id issue-summary)}
         [issue-panel issue-summary])]]]))
