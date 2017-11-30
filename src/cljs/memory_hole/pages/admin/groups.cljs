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
  (let [default-group (if js/ldap
                        {:group-name ""
                         :group-id ""}
                        {:group-name ""})]
    (r/with-let [group (r/atom default-group)]
      [:div.form-horizontal
       [:legend "Add Group"]
       [bs/FormGroup
        [bs/ControlLabel
         {:class "col-lg-2"}
         "Group Name"]
        [:div.col-lg-10
         [bs/FormControl
          {:type        :text
           :value       (:group-name @group)
           :on-change   #(swap! group assoc :group-name (-> % .-target .-value))
           :placeholder "Enter group name"}]]
        (when js/ldap
          [bs/ControlLabel
           {:class "col-lg-2"}
           "Distinguished Name"])
        (when js/ldap
          [:div.col-lg-10
           [bs/FormControl
            {:type        :text
             :value       (:group-id @group)
             :on-change   #(swap! group assoc :group-id (-> % .-target .-value))
             :placeholder "Enter LDAP distinguished name"}]])]
       [save-button (-> @group
                        (update :group-name string/trim))
        #(reset! group default-group)]])))

(defn group-info [{:keys [group-name group-id] :as group}]
  ;; EWWWWWWWWWWWWWWWWWWWWWWWWWWWW
  (dispatch [:admin/load-group-users group-name])
  [bs/ListGroupItem
   [:div
    [:div
     [:b group-name]]
    [:div
     [:code group-id]]
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
  [:div
   [:div.row
    [:div.col-sm-12
     [add-group]]]
   [:hr]
   [:legend "Current Groups"]
   [group-list]])
