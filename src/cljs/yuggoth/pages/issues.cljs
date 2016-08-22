(ns yuggoth.pages.issues
  (:require [reagent.core :as r]
            [clojure.set :refer [difference rename-keys]]
            [re-frame.core :refer [dispatch subscribe]]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea single-dropdown]]
            [re-com.splits
             :refer [hv-split-args-desc]]
            [yuggoth.bootstrap :as bs]
            [yuggoth.routes :refer [set-location!]]
            [clojure.string :as s]))

(def rounded-panel (flex-child-style "1"))

(defn highlight-code [node]
  (let [nodes (.querySelectorAll (r/dom-node node) "pre code")]
    (loop [i (.-length nodes)]
      (when-not (neg? i)
        (when-let [item (.item nodes i)]
          (.highlightBlock js/hljs item))
        (recur (dec i))))))

(defn markdown-component []
  (r/create-class
    {:component-did-mount
     #(highlight-code (r/dom-node %))
     :component-did-update
     #(highlight-code (r/dom-node %))
     :reagent-render
     (fn [content]
       [:div.view-issue-detail
        {:dangerouslySetInnerHTML
         {:__html (-> content str js/marked)}}])}))

(defn preview-panel [text]
  [box
   :size "atuo"
   :class "edit-issue-detail"
   :child
   [:div.rounded-panel {:style rounded-panel}
    [markdown-component text]]])

(defn edit-panel [text]
  [box
   :size "atuo"
   :child
   [bs/FormControl
    {:component-class "textarea"
     :class "edit-issue-detail"
     :placeholder "issue detail"
     :value       @text
     :on-change   #(reset! text (-> % .-target .-value))}]])

(defn control-buttons [issue]
  (let [new-issue? (:support-issue-id issue)]
    [:div.row>div.col-sm-12
     [bs/Button
      {:bs-style "danger"
       :on-click #(set-location! (if new-issue? "#/" "#/view-issue"))}
      "Cancel"]
     [bs/Button
      {:bs-style "primary pull-right"
       :on-click #(if new-issue?
                   (dispatch [:create-issue @issue])
                   (dispatch [:save-issue @issue]))}
      "Save"]]))

(defn tag-selector [tags]
  (r/with-let [avilable-tags (->> @(subscribe [:tags])
                                  (map #(rename-keys % {:tag-id :id :tag :label}))
                                  (set))
               tags-by-id    (group-by :id avilable-tags)
               selected      (r/atom nil)]
    [:div
     [single-dropdown
      :choices (vec (difference avilable-tags @tags))
      :model selected
      :width "300px"
      :max-height "400px"
      :filter-box? true
      :on-change #(reset! selected %)]
     [:span "selected: " (str @selected)]
     [bs/Button
      {:bs-style "primary"
       :on-click #(swap! tags conj (-> @selected
                                       tags-by-id
                                       first
                                       (rename-keys {:id :tag-id :label :tag})))}
      "add tag"]]))

(defn remove-tag [tags tag-id]
  (remove #(= tag-id (:tag-id %)) tags))

(defn selected-tags [tags]
  [:ul
   (for [{:keys [tag-id tag]} @tags]
     ^{:key tag-id}
     [:li tag " "
      [:a
       {:on-click #(swap! tags remove-tag tag-id)}
       [:span.glyphicon.glyphicon-remove]]])])

(defn create-tag []
  (r/with-let [tag (r/atom nil)]
    [:div
     [:input
      {:type      :text
       :on-change #(reset! tag (-> % .-target .-value))}]
     [bs/Button
      {:bs-style "primary"
       :on-click #(dispatch [:create-tag @tag])}
      "create tag"]]))

(defn tag-editor [tags]
  [:div
   [:div.row>div.col-md-12
    [tag-selector tags]]
   [:div.row>div.col-md-12
    [selected-tags tags]]
   [:div.row>div.col-md-12
    [create-tag]]])

(defn edit-issue-page []
  (r/with-let [issue   (r/atom (-> @(subscribe [:issue])
                                   (update :title #(or % ""))
                                   (update :summary #(or % ""))
                                   (update :detail #(or % ""))
                                   (update :tags #(set (or % [])))))
               title   (r/cursor issue [:title])
               summary (r/cursor issue [:summary])
               detail  (r/cursor issue [:detail])
               tags    (r/cursor issue [:tags])]
    [v-box
     :size "auto"
     :gap "10px"
     :height "auto"
     :children
     [[control-buttons issue]
      [input-text
       :model title
       :class "edit-issue-title"
       :placeholder "title of the issue"
       :on-change #(reset! title %)]
      [input-text
       :model summary
       :width "100%"
       :placeholder "issue summary"
       :on-change #(reset! summary %)]
      [tag-editor tags]
      [h-split
       :panel-1 [edit-panel detail]
       :panel-2 [preview-panel @detail]
       :size "auto"]
      [control-buttons issue]]]))

(defn confirm-delete-modal [confirm-open? support-issue-id]
  [bs/Modal {:show @confirm-open?}
   [bs/Modal.Header
    [bs/Modal.Title "Are you sue you wish to delete the issue?"]]
   [bs/Modal.Body
    [bs/Button {:bs-style "danger"
                :on-click #(reset! confirm-open? false)}
     "Cancel"]
    [bs/Button {:bs-style "primary pull-right"
                :on-click #(do
                            (reset! confirm-open? false)
                            (dispatch [:delete-issue support-issue-id]))}
     "Delete"]]])

(defn delete-issue [{:keys [support-issue-id]}]
  (r/with-let [confirm-open? (r/atom false)]
    [:div.pull-left
     [confirm-delete-modal confirm-open? support-issue-id]
     [bs/Button {:bs-style "danger" :on-click #(reset! confirm-open? true)} "delete"]]))

(defn format-tags [tags]
  [:div (map (fn [{:keys [tag]}] [bs/Badge tag]) tags)])

(defn view-issue-page []
  (let [issue (subscribe [:issue])]
    [:div
     [:div.row
      [:div.col-sm-12 [:h2 (:title @issue)]]
      [:div.col-sm-12 (format-tags (:tags @issue))]
      [:div.col-sm-12 [:p (:summary @issue)]]
      [:div.col-sm-12 [markdown-component (:detail @issue)]]]
     [:div.row>div.col-sm-12
      [delete-issue @issue]
      [bs/Button
       {:bs-style "primary"
        :class    "pull-right"
        :on-click #(set-location! "#/edit-issue")}
       "edit"]]]))
