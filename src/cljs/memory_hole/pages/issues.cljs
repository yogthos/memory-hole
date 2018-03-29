(ns memory-hole.pages.issues
  (:require [cljsjs.showdown]
            [clojure.set :refer [difference rename-keys]]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [re-com.core
             :refer [box v-box h-split v-split title flex-child-style input-text input-textarea single-dropdown]]
            [memory-hole.datetime :as dt]
            [re-com.splits
             :refer [hv-split-args-desc]]
            [memory-hole.attachments :refer [upload-form]]
            [memory-hole.bootstrap :as bs]
            [memory-hole.pages.common :refer [validation-modal confirm-modal]]
            [memory-hole.routes :refer [href navigate!]]
            [memory-hole.validation :as v]
            [memory-hole.widgets.tag-editor :refer [tag-editor]]))

(def rounded-panel (flex-child-style "1"))

(defn highlight-code [node]
  (let [nodes (.querySelectorAll (r/dom-node node) "pre code")]
    (loop [i (.-length nodes)]
      (when-not (neg? i)
        (when-let [item (.item nodes i)]
          (.highlightBlock js/hljs item))
        (recur (dec i))))))

(defn markdown-preview []
  (let [md-parser (js/showdown.Converter.)]
    (r/create-class
      {:component-did-mount
       #(highlight-code (r/dom-node %))
       :component-did-update
       #(highlight-code (r/dom-node %))
       :reagent-render
       (fn [content]
         [:div
          {:dangerouslySetInnerHTML
           {:__html (.makeHtml md-parser (str content))}}])})))

(defn preview-panel [text]
  [box
   :size "auto"
   :class "issue-detail"
   :child
   [:div.rounded-panel {:style rounded-panel}
    [markdown-preview text]]])

(defn editor [text]
  (r/create-class
    {:component-did-mount
     #(let [editor (js/SimpleMDE.
                     (clj->js
                       {:autofocus    true
                        :spellChecker false
                        :placeholder  "Issue details"
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
   :class "issue-detail"
   :size "auto"
   :child [:div.issue-detail [editor text]]])

(defn select-issue-keys [issue]
  (let [issue-keys [:title :tags :summary :detail]]
    (select-keys (update issue :tags set) issue-keys)))

(defn issue-empty? [issue]
  (->> issue
       (keep #(-> % second not-empty))
       (empty?)))

(defn issue-updated? [original-issue edited-issue]
  (if (:support-issue-id edited-issue)
    (not= (select-issue-keys original-issue)
          (select-issue-keys edited-issue))
    (not (issue-empty? edited-issue))))

(defn delete-issue-button [{:keys [support-issue-id]}]
  (r/with-let [confirm-open? (r/atom false)]
    [bs/Button
     {:bs-style "danger"
      :on-click #(reset! confirm-open? true)}
     "Delete"
     [confirm-modal
      "Are you sure you wish to delete the issue?"
      confirm-open?
      #(dispatch [:delete-issue support-issue-id])
      "Delete"]]))

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
     [validation-modal "Missing required fields" errors]
     [:div.btn-toolbar.pull-right
      [bs/Button
       {:bs-style "warning"
        :on-click #(if (issue-updated? @original-issue @edited-issue)
                    (reset! confirm-open? true)
                    (cancel-edit))}
       "Cancel"]
      [bs/Button
       {:bs-style   "success"
        :on-click   #(when-not (reset! errors (v/validate-issue @edited-issue))
                      (if issue-id
                        (dispatch [:save-issue @edited-issue])
                        (dispatch [:create-issue @edited-issue])))}
       "Save"]]]))

(defn render-tags [tags]
  [:div.btn-toolbar
   (for [tag tags]
     ^{:key tag}
     [:button.btn.btn-xs.btn-default
      {:on-click #(navigate! (str "/issues/" tag))}
      tag])])

(defn attachment-list [support-issue-id files]
  (r/with-let [confirm-open? (r/atom false)
               action        (r/atom nil)]
    (when-not (empty? files)
      [:div
       [confirm-modal
        "Are you sure you wish to delete this file?"
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
      "Attach File"]
     [upload-form
      support-issue-id
      open?
      (fn [{:keys [name]}]
        (dispatch [:attach-file name]))]]))

(defn issue-detail-pane [detail]
  (r/with-let [preview (r/atom :split)]
    [:div
     [bs/ControlLabel "Issue Details"]
     [:span.pull-right
      {:style {:display "float"}}
      [:a.editor-view-button
       {:class    (if (= :edit @preview) "active")
        :on-click #(reset! preview :edit)}
       [:span.fa.fa-pencil-square-o]]
      [:a.editor-view-button
       {:class    (if (= :split @preview) "active")
        :on-click #(reset! preview :split)}
       [:span.fa.fa-columns]]
      [:a.editor-view-button
       {:class    (if (= :preview @preview) "active")
        :on-click #(reset! preview :preview)}
       [:span.fa.fa-file-text-o]]]
     (case @preview
       :split
       [h-split
        :class "issue-detail"
        :panel-1 [edit-panel detail]
        :panel-2 [preview-panel @detail]
        :size "auto"]
       :preview
       [preview-panel @detail]
       :edit
       [edit-panel detail])]))

(defn edit-issue-page []
  (r/with-let [original-issue (subscribe [:issue])
               group-list #(mapv (fn [{:keys [group-name group-id]}] {:id group-id :label group-name}) @(subscribe [:groups]))
               edited-issue   (-> @original-issue
                                  (update :title #(or % ""))
                                  (update :summary #(or % ""))
                                  (update :detail #(or % ""))
                                  (update :tags #(set (or % [])))
                                  r/atom)
               title          (r/cursor edited-issue [:title])
               summary        (r/cursor edited-issue [:summary])
               detail         (r/cursor edited-issue [:detail])
               group          (r/cursor edited-issue [:group-id])
               tags           (r/cursor edited-issue [:tags])]
    [v-box
     :size "auto"
     :gap "10px"
     :height "auto"
     :children
     [[:div.row
       [:div.col-sm-6
        [:h3.page-title (if @original-issue "Edit Issue" "Add Issue")]]
       [:div.col-sm-6
        [control-buttons original-issue edited-issue]]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Title"]
       [input-text
        :model title
        :width "100%"
        :class "edit-issue-title"
        :placeholder "Title of the issue"
        :on-change #(reset! title %)]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Summary"]
       [input-text
        :model summary
        :width "100%"
        :placeholder "Issue summary"
        :on-change #(reset! summary %)]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Tags"]
       [tag-editor tags]]
      [bs/FormGroup
       [bs/ControlLabel "Issue Group"]
       [single-dropdown
        :model group
        :choices (group-list)
        :width "100%"
        :placeholder "Issue Group"
        :on-change #(reset! group %)]]
      [:div.row>div.col-sm-12
       [issue-detail-pane detail]]
      [:div.row
       [:div.col-sm-6
        (when-let [support-issue-id (:support-issue-id @edited-issue)]
          [attachment-component support-issue-id (r/cursor original-issue [:files])])]
       [:div.col-sm-6
        [control-buttons original-issue edited-issue]]]]]))

(defn view-issue-page []
  (let [issue (subscribe [:issue])
        format-date (fn [date]
                      (when-not (nil? date) (dt/format-date date)))]
    [:div.row>div.col-sm-12
     [bs/Panel
      {:class "view-issue-panel"}
      [:div.row
       [:div.col-sm-12>h2
        (:title @issue)
        [:span.pull-right [bs/Badge (str (:views @issue))]]]
       [:div.col-sm-12>p "Group: " (:group-name @issue)]
       [:div.col-sm-12>p (:summary @issue)]
       [:div.col-sm-12.padded-bottom (render-tags (:tags @issue))]
       [:div.col-sm-12>p
        "Last updated by: "
        (:updated-by-screenname @issue)
        " on " (format-date (:update-date @issue))]
       [:div.col-sm-12>hr]
       [:div.col-sm-12 [markdown-preview (:detail @issue)]]
       [:div.col-sm-12>hr]
       [:div.col-sm-6
        [attachment-component
         (:support-issue-id @issue)
         (r/cursor issue [:files])]]
       [:div.col-sm-6
        [:div.btn-toolbar.pull-right
         [delete-issue-button @issue]
         [:a.btn.btn-primary
          (href "/edit-issue") "Edit"]]]]]]))

