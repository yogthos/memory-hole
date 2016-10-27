(ns memory-hole.pages.issues
  (:require [reagent.core :as r]
            [clojure.set :refer [difference rename-keys]]
            [re-frame.core :refer [dispatch subscribe]]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea single-dropdown]]
            [memory-hole.datetime :as dt]
            [re-com.splits
             :refer [hv-split-args-desc]]
            [memory-hole.routes :refer [href navigate!]]
            [memory-hole.validation :as v]
            [memory-hole.bootstrap :as bs]
            [memory-hole.pages.common :refer [spacer validation-modal confirm-modal]]
            [memory-hole.attachments :refer [upload-form]]
            [clojure.string :as s]
            [cljsjs.showdown]))

(def rounded-panel (flex-child-style "1"))
(def key-enter 13)
(def key-backspace 8)

(defn highlight-code [node]
  (let [nodes (.querySelectorAll (r/dom-node node) "pre code")]
    (loop [i (.-length nodes)]
      (when-not (neg? i)
        (when-let [item (.item nodes i)]
          (.highlightBlock js/hljs item))
        (recur (dec i))))))

(defn markdown-component []
  (let [md-parser (js/showdown.Converter.)]
    (r/create-class
      {:component-did-mount
       #(highlight-code (r/dom-node %))
       :component-did-update
       #(highlight-code (r/dom-node %))
       :reagent-render
       (fn [content]
         [:div.view-issue-detail
          {:dangerouslySetInnerHTML
           {:__html (.makeHtml md-parser (str content))}}])})))

(defn preview-panel [text]
  [box
   :size "atuo"
   :class "edit-issue-detail"
   :child
   [:div.rounded-panel {:style rounded-panel}
    [markdown-component text]]])

(defn editor [text]
  (r/create-class
    {:component-did-mount
     #(let [editor (js/SimpleMDE.
                     (clj->js
                       {:autofocus    true
                        :placeholder  "issue detail"
                        :toolbar      ["bold"
                                       "italic"
                                       "strikethrough"
                                       "|"
                                       "heading"
                                       "code"
                                       "quote"
                                       "|"
                                       "unordered-list"
                                       "ordered-list"
                                       "|"
                                       "link"
                                       "image"]
                        :element      (r/dom-node %)
                        :initialValue @text}))]
       (-> editor .-codemirror (.on "change" (fn [] (reset! text (.value editor))))))
     :reagent-render
     (fn [] [:textarea])}))

(defn edit-panel [text]
  [box
   :size "atuo"
   :child [:div.edit-issue-detail [editor text]]])

(defn select-issue-keys [issue]
  (let [issue-keys [:title :tags :summary :detail]]
    (select-keys (update issue :tags set) issue-keys)))

(defn issue-updated? [original-issue edited-issue]
  (or (nil? (:support-issue-id edited-issue))
      (= (select-issue-keys original-issue)
         (select-issue-keys edited-issue))))

(defn control-buttons [original-issue edited-issue]
  (r/with-let [issue-id      (:support-issue-id @edited-issue)
               errors        (r/atom nil)
               confirm-open? (r/atom false)
               cancel-edit   #(navigate!
                               (if issue-id (str "/issue/" issue-id) "/"))]
    [:div.row>div.col-sm-12
     [confirm-modal
      "Discard changes?"
      confirm-open?
      cancel-edit
      "Discard"]
     [validation-modal errors]
     [:div.pull-right
      [bs/Button
       {:bs-style "danger"
        :on-click #(if (issue-updated? @original-issue @edited-issue)
                    (cancel-edit)
                    (reset! confirm-open? true))}
       "Cancel"]
      spacer
      [bs/Button
       {:bs-style   "primary"
        :pull-right true
        :on-click   #(when-not (reset! errors (v/validate-issue @edited-issue))
                      (if issue-id
                        (dispatch [:save-issue @edited-issue])
                        (dispatch [:create-issue @edited-issue])))}
       "Save"]]]))

(defn render-tags [tags]
  [:span
   (for [tag tags]
     ^{:key tag}
     [bs/Label
      {:style {:margin-right "5px"}}
      tag])])

(defn remove-tag [tags tag]
  (->> tags
       (remove #(= % tag))
       vec))

(defn add-tag [tags new-tag]
  (when (seq (s/trim @new-tag))
    (swap! tags conj @new-tag)
    (reset! new-tag nil)))

(defn tag-input [tags]
  (r/with-let [new-tag (r/atom nil)]
    [:div
     [:div.bootstrap-tagsinput
      (for [tag @tags]
        ^{:key tag}
        [:span.tag.label.label-info
         tag
         [:span {:data-role "remove"
                 :on-click  #(swap! tags remove-tag tag)}]])
      [:input
       {:type        :text
        :value       @new-tag
        :on-change   #(reset! new-tag (-> % .-target .-value s/lower-case))
        :on-blur     #(add-tag tags new-tag)
        :on-key-down #(condp = (.-keyCode %)
                       key-enter (add-tag tags new-tag)
                       key-backspace (if (s/blank? @new-tag) (swap! tags (comp vec butlast)))
                       "Default")}]]

     (when-let [new-tags (-> (set @tags)
                             (difference (set (map :tag @(subscribe [:tags]))))
                             (not-empty))]
       [:div "Creating tags: "
        (for [tag new-tags]
          ^{:key tag}
          [bs/Label {:bs-style "danger"
                     :style    {:margin-right "5px"}}
           tag])])]))

(defn tag-editor [tags]
  [:div.row
   [:div.col-sm-12
    [tag-input tags]]])

(defn attachment-list [support-issue-id files]
  (r/with-let [confirm-open? (r/atom false)
               action        (r/atom nil)]
    (when-not (empty? files)
      [:div
       [confirm-modal
        "Are you sue you wish to delete this file?"
        confirm-open?
        @action
        "Delete"]
       [:h4 "Attachments"]
       [:hr]
       [:ul
        (for [[idx file] (map-indexed vector files)]
          ^{:key idx}
          [:li
           [:a (href (str "/api/file/" support-issue-id "/" file)) file]
           " "
           [:span.glyphicon.glyphicon-remove
            {:style    {:color "red"}
             :on-click (fn []
                         (reset! action #(dispatch [:delete-file support-issue-id file]))
                         (reset! confirm-open? true))}]])]])))

(defn attachment-component [support-issue-id files]
  (r/with-let [open? (r/atom false)]
    [:div
     [attachment-list support-issue-id @files]
     [bs/Button
      {:on-click #(reset! open? true)}
      "attach file"]
     [upload-form
      support-issue-id
      open?
      (fn [{:keys [name]}]
        (dispatch [:attach-file name]))]]))

(defn edit-issue-page []
  (r/with-let [original-issue (subscribe [:issue])
               edited-issue   (-> @original-issue
                                  (update :title #(or % ""))
                                  (update :summary #(or % ""))
                                  (update :detail #(or % ""))
                                  (update :tags #(set (or % [])))
                                  r/atom)
               title          (r/cursor edited-issue [:title])
               summary        (r/cursor edited-issue [:summary])
               detail         (r/cursor edited-issue [:detail])
               tags           (r/cursor edited-issue [:tags])]
    [v-box
     :size "auto"
     :gap "10px"
     :height "auto"
     :children
     [[control-buttons original-issue edited-issue]
      [bs/FormGroup
       [bs/ControlLabel "Issue Title"]
       [input-text
        :model title
        :width "100%"
        :class "edit-issue-title"
        :placeholder "title of the issue"
        :on-change #(reset! title %)]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Summary"]
       [input-text
        :model summary
        :width "100%"
        :placeholder "issue summary"
        :on-change #(reset! summary %)]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Tags"]
       [tag-editor tags]]
      [bs/ControlLabel "Issue Detail"]
      [h-split
       :class "issue-editor"
       :panel-1 [edit-panel detail]
       :panel-2 [preview-panel @detail]
       :size "auto"]
      (when-let [support-issue-id (:support-issue-id @edited-issue)]
        [attachment-component support-issue-id (r/cursor original-issue [:files])])
      [control-buttons original-issue edited-issue]]]))

(defn delete-issue [{:keys [support-issue-id]}]
  (r/with-let [confirm-open? (r/atom false)]
    [:div.pull-left
     [confirm-modal
      "Are you sue you wish to delete the issue?"
      confirm-open?
      #(dispatch [:delete-issue support-issue-id])
      "Delete"]
     [bs/Button {:bs-style "danger"
                 :on-click #(reset! confirm-open? true)}
      "Delete"]]))

(defn view-issue-page []
  (let [issue (subscribe [:issue])]
    [:div.row>div.col-sm-12
     [bs/Panel
      {:class "view-issue-panel"}
      [:div.row
       [:div.col-sm-12>h2
        (:title @issue)
        [:span.pull-right [bs/Badge (str (:views @issue))]]]
       [:div.col-sm-12>p (:summary @issue)]
       [:div.col-sm-12>h4 (render-tags (:tags @issue))]
       [:div.col-sm-12>p
        "last updated by: "
        (:updated-by-screenname @issue)
        " on " (dt/format-date (:update-date @issue))]
       [:div.col-sm-12>hr]
       [:div.col-sm-12 [markdown-component (:detail @issue)]]
       [:div.col-sm-12
        [attachment-component
         (:support-issue-id @issue)
         (r/cursor issue [:files])]]
       [:div.col-sm-12>hr]
       [:div.col-sm-12>div.pull-right
        [bs/FormGroup
         [delete-issue @issue]
         spacer
         [:a.btn.btn-primary
          (href "/edit-issue") "Edit"]]]]]]))

