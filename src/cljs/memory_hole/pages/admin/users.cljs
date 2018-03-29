(ns memory-hole.pages.admin.users
  (:require [memory-hole.bootstrap :as bs]
            [re-frame.core :refer [dispatch subscribe]]
            [memory-hole.key-events :refer [on-enter]]
            [memory-hole.pages.common :refer [validation-modal]]
            [memory-hole.validation :as v]
            [reagent.core :as r]))

(defn user-search []
  (r/with-let [search    (r/atom nil)
               do-search #(when-let [value (not-empty @search)]
                            (dispatch [:admin/search-for-users value]))]
    [bs/FormGroup
     [bs/InputGroup
      [bs/FormControl
       {:type        "text"
        :class       "input-sm"
        :placeholder "Type in a user name to see their details"
        :on-change   #(reset! search (-> % .-target .-value))
        :on-key-down #(on-enter % do-search)}]
      [bs/InputGroup.Button
       [:button.btn.btn-sm.btn-default
        {:on-click do-search}
        "Search"]]]]))

(defn control-buttons [user close-editor]
  (r/with-let [errors  (r/atom nil)
               user-id (:user-id @user)]
    [:div.row>div.col-sm-12
     [validation-modal "Missing required fields" errors]
     [:div.pull-right
      [:div.btn-toolbar
       [:button.btn.btn-sm.btn-danger
        {:on-click close-editor}
        "Cancel"]
       [:button.btn.btn-sm.btn-success.pull-right
        {:on-click   #(let [new-user? (nil? user-id)]
                        (when-not (reset! errors
                                          ((if new-user?
                                             v/validate-create-user
                                             v/validate-update-user)
                                            @user))
                          (dispatch
                            [(if new-user?
                               :admin/create-user-profile
                               :admin/update-user-profile)
                             (update @user :belongs-to (fn [belongs-to] (map :group-id belongs-to)))])
                          (close-editor)))}
        "Save"]]]]))

(defn field-group [label cursor type placeholder]
  [bs/FormGroup
   [bs/ControlLabel
    {:class "col-lg-2"}
    label]
   [:div.col-lg-10
    [bs/FormControl
     {:type        type
      :value       (or @cursor "")
      :on-change   #(reset! cursor (-> % .-target .-value))
      :placeholder placeholder}]]])

(defn group-selector [selected-group & [ignored-groups]]
  (r/with-let [groups @(subscribe [:groups])]
    [bs/DropdownButton
     {:id     "group-selector"
      :title  (:group-name @selected-group "Select Group")
      :bsSize "small"}
     (->> groups
          (remove (if-let [ignore (not-empty (set @ignored-groups))]
                    (fn [group] (ignore group))
                    (fn [_] false)))
          (map-indexed
            (fn [idx {:keys [group-id group-name] :as group}]
              ^{:key group-id}
              [bs/MenuItem
               {:id       group-id
                :on-click #(reset! selected-group group)}
               group-name]))
          (doall))]))

(defn group-filter [groups]
  (fn [group-or-group-id]
    (if (string? group-or-group-id)
      (some
        (fn [{:keys [group-id] :as g}]
          (when (= group-or-group-id group-id) g))
        groups)
      group-or-group-id)))

(defn group-list-selector [group-list]
  ;; GROSS
  (swap! group-list (partial map (group-filter @(subscribe [:groups]))))
  [:div
   (into [:div.list-group]
         (map (fn [group] [bs/Button
                           {:class    "btn-danger"
                            :on-click #(swap! group-list (fn [l] (remove (partial = group) l)))}
                           (:group-name group) " x"])
              @group-list))
   (let [group (r/atom nil)]
     [bs/ButtonGroup
      [group-selector group group-list]
      [bs/Button {:on-click (fn []
                              (swap! group-list conj @group)
                              (reset! group nil))
                  :bsSize   "small"
                  :class    "btn-success"} "Add to Group"]])])

(defn edit-user [title user-map close-editor]
  (r/with-let [user (-> user-map
                        (dissoc :last-login)
                        (update :pass identity)
                        (update :pass-confirm identity)
                        (update :admin boolean)
                        (update :is-active boolean)
                        r/atom)]
    [:div.form-horizontal
     [:legend title]
     [field-group
      "Screen Name"
      (r/cursor user [:screenname])
      :text "Enter screen name for the user"]
     [field-group
      "Password"
      (r/cursor user [:pass])
      :password
      (if (:last-login user-map)
        "Enter the password for the user (leave empty to keep the existing password)"
        "Enter the password for the user")]
     [field-group
      "Confirm password"
      (r/cursor user [:pass-confirm])
      :password "Confirm the password for the user"]
     [bs/FormGroup
      [bs/ControlLabel
       {:class "col-lg-2"}
       "Group"]
      [:div.col-lg-10
       [group-list-selector (r/cursor user [:belongs-to])]]]
     [bs/FormGroup
      [:span.col-lg-2]
      [:div.col-lg-10
       [bs/Checkbox
        {:checked   (boolean (:admin @user))
         :on-change #(swap! user update :admin not)}
        "Admin"]
       [bs/Checkbox
        {:checked   (boolean (:is-active @user))
         :on-change #(swap! user update :is-active not)}
        "Active"]]]
     [control-buttons user close-editor]]))

(defn user-info [user-map]
  (r/with-let [expanded? (r/atom false)]
    [bs/ListGroupItem
     (if @expanded?
       [edit-user "Edit User" user-map #(reset! expanded? false)]
       [:div
        [:span (:screenname user-map)]
        [:button.btn.btn-xs.btn-primary.pull-right
         {:on-click #(swap! expanded? not)}
         "Edit"]])]))

(defn user-list []
  (let [users (subscribe [:admin/users])]
    (when-not (empty? @users)
      [bs/ListGroup
       (for [user @users]
         ^{:key (:user-id user)}
         [user-info user])])))

(defn users-page []
  (r/with-let [show-new-user-form? (r/atom false)]
    (if @show-new-user-form?
      [:div.row
       [:div.col-sm-12
        [edit-user "Add User" {} #(reset! show-new-user-form? false)]]]
      [:div
       [:div.row
        [:div.col-sm-10 [user-search]]
        [:div.col-sm-2
         [:button.btn.btn-sm.btn-success.pull-right
          {:on-click #(reset! show-new-user-form? true)}
          "Add new user"]]]
       [user-list]])))

