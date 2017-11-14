(ns memory-hole.pages.admin.groups
  (:require [cuerdas.core :as string]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [memory-hole.bootstrap :as bs]
            [memory-hole.key-events :refer [on-enter]]
            [memory-hole.pages.common :refer [validation-modal]]
            [memory-hole.validation :as v]))

(defn save-button [group close-editor]
  (r/with-let [errors (r/atom nil)]
    [:div.row>div.col-sm-12
     [validation-modal "Invalid group name" errors]
     [:div.pull-right
      [:div.btn-toolbar
       [:button.btn.btn-sm.btn-success
        {:pull-right true
         :on-click   (fn []
                       (let [other-groups (->> @(subscribe [:groups])
                                               (map :group-name))]
                         (when-not (reset! errors (v/validate-group group other-groups))
                           (dispatch [:admin/create-group group])
                           (close-editor))))}
        "Save"]]]]))

(defn add-group []
  (r/with-let [group-name (r/atom "")]
    [:div.form-horizontal
     [:legend "Add Group"]
     [bs/FormGroup
      [bs/ControlLabel
       {:class "col-lg-2"}
       "Group Name"]
      [:div.col-lg-10
       [bs/FormControl
        {:type        :text
         :value       @group-name
         :on-change   #(reset! group-name (-> % .-target .-value))
         :placeholder "Enter group name"}]]]
     [save-button {:group-name (string/trim @group-name)} #(reset! group-name "")]]))

(defn group-info [{:keys [group-name] :as group}]
  (dispatch [:admin/load-group-users group-name])
  [bs/ListGroupItem
   [:div
    [:div
     [:b group-name]]
    (if-let [users (not-empty @(subscribe [:admin/group-users group-name]))]
      [:div
       [:ul
        (for [user users]
          ^{:key (:user-id user)}
          [:li (:screenname user)])]])]])

(defn group-list []
  (let [groups (subscribe [:groups])]
    (when-not (empty? @groups)
      [bs/ListGroup
       (for [group @groups]
         ^{:key (:group-id group)}
         [group-info group])])))

(defn groups-page []
  (r/with-let [show-new-group-form? (r/atom false)]
    [:div
     [:div.row
      [:div.col-sm-12
       [add-group]]]
     [:hr]
     [:legend "Current Groups"]
     [group-list]]))
