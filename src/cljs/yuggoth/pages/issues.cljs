(ns yuggoth.pages.issues
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea]]
            [re-com.splits
             :refer [hv-split-args-desc]]))

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
   :child
   [:div.rounded-panel {:style rounded-panel}
    [markdown-component text]]])

(defn edit-panel [text]
  [box
   :size "atuo"
   :child
   [:textarea.form-control.edit-issue-detail
    {:on-change   #(reset! text (-> % .-target .-value))
     :value       @text
     :placeholder "describe the issue"}]])

(defn view-issue-page []
  (let [issue (subscribe [:issue])]
    [:div.row
     [:div.col-sm-12 [:h2 (:title @issue)]]
     [:div.col-sm-12 [:h4 (str (:tags @issue))]]
     [:div.col-sm-12 [:p (:summary @issue)]]
     [:div.col-sm-12 [markdown-component (:detail @issue)]]]))

(defn control-buttons [issue]
  [:div.row>div.col-sm-12
   [:button.btn.btn-danger
    {:on-click #(dispatch [:cancel-issue-edit])}
    "Cancel"]
   [:button.btn.btn-primary.pull-right
    {:on-click #(dispatch [:save-issue @issue])}
    "Save"]])

(defn edit-issue-page []
  (r/with-let [issue   (r/atom (-> @(subscribe [:issue])
                                   (update :title #(or % ""))
                                   (update :summary #(or % ""))
                                   (update :detail #(or % ""))))
               title   (r/cursor issue [:title])
               summary (r/cursor issue [:summary])
               detail  (r/cursor issue [:detail])]
    [v-box
     :size "auto"
     :gap "10px"
     :height "auto"
     :children
     [[control-buttons issue]
      [input-text
       :model title
       :class "edit-issue-title"
       :validation-regex #"^(?!\s*$).+"
       :placeholder "title of the issue"
       :on-change #(reset! title %)]
      [input-text
       :model summary
       :width "100%"
       :validation-regex #"^(?!\s*$).+"
       :placeholder "issue summary"
       :on-change #(reset! summary %)]
      [:div.row>div.col-md-12>p "tags" (:tags @issue)]
      [h-split
       :panel-1 [edit-panel detail]
       :panel-2 [preview-panel @detail]
       :size "auto"]
      [control-buttons issue]]]))