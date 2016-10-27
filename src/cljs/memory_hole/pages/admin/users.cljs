(ns memory-hole.pages.admin.users
  (:require [memory-hole.bootstrap :as bs]
            [re-frame.core :refer [dispatch subscribe]]
            [memory-hole.key-events :refer [on-enter]]
            [memory-hole.pages.common :refer [spacer validation-modal]]
            [memory-hole.validation :as v]
            [reagent.core :as r]))

(defn issue-search []
  (r/with-let [search    (r/atom nil)
               do-search #(when-let [value (not-empty @search)]
                           (dispatch [:admin/search-for-users value]))]
    [bs/FormGroup
     [bs/InputGroup
      [bs/FormControl
       {:type        "text"
        :placeholder "Type in a user name to see user details"
        :on-change   #(reset! search (-> % .-target .-value))
        :on-key-down #(on-enter % do-search)}]
      [bs/InputGroup.Button
       [bs/Button
        {:on-click do-search}
        "Search"]]]]))

(defn control-buttons [user close-editor]
  (r/with-let [errors  (r/atom nil)
               user-id (:user-id @user)]
    [:div.row>div.col-sm-12
     [validation-modal errors]
     [:div.pull-right
      [bs/Button
       {:bs-style "danger"
        :on-click close-editor}
       "Cancel"]
      [:span {:style {:margin-right "5px"}}]
      [bs/Button
       {:bs-style   "primary"
        :pull-right true
        :on-click   #(let [new-user? (nil? user-id)]
                      (when-not (reset! errors
                                        ((if new-user?
                                           v/validate-create-user
                                           v/validate-update-user)
                                          @user))
                        (dispatch
                          [(if new-user?
                             :admin/create-user-profile
                             :admin/update-user-profile)
                           @user])
                        (close-editor)))}
       "Save"]]]))

(defn field-group [label cursor type placeholder]
  [bs/FormGroup
   {:class "form-horizontal"}
   [bs/ControlLabel label]
   [bs/FormControl
    {:type        type
     :value       (or @cursor "")
     :on-change   #(reset! cursor (-> % .-target .-value))
     :placeholder placeholder}]])

(defn edit-user [user-map close-editor]
  (r/with-let [user (-> user-map
                        (dissoc :last-login)
                        (update :pass identity)
                        (update :pass-confirm identity)
                        (update :admin boolean)
                        (update :is-active boolean)
                        r/atom)]
    [:div
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
     [bs/Checkbox
      {:checked   (boolean (:admin @user))
       :on-change #(swap! user update :admin not)}
      "Admin"]
     [bs/Checkbox
      {:checked   (boolean (:is-active @user))
       :on-change #(swap! user update :is-active not)}
      "Active"]
     [control-buttons user close-editor]]))

(defn user-info [user-map]
  (r/with-let [expanded? (r/atom false)]
    [bs/ListGroupItem
     (if @expanded?
       [edit-user user-map #(reset! expanded? false)]
       [:div
        [:span (:screenname user-map)]
        [bs/Label
         {:bs-style "primary"
          :class    "pull-right edit-user"
          :on-click #(swap! expanded? not)}
         "Edit"]])]))

(defn user-list []
  (let [users (subscribe [:admin/users])]
    (when-not (empty? @users)
      [bs/ListGroup
       (for [user @users]
         ^{:key (:user-id user)}
         [user-info user])])))

(defn add-user-form []
  (r/with-let [show-add-user-menu? (r/atom false)]
    [:div
     (when-not @show-add-user-menu?
       [:div.row
        [:div.col-sm-10 [issue-search]]
        [:div.col-sm-2>div.pull-right
         [bs/Button
          {:bs-style "primary"
           :on-click #(reset! show-add-user-menu? true)}
          "Add new user"]]])
     (when @show-add-user-menu?
       [:div.row
        [:div.col-sm-12 [:h3 "Add User"]]
        [:div.col-sm-12
         [edit-user {} #(reset! show-add-user-menu? false)]]])]))

(defn users-page []
  [:div
   [add-user-form]
   [user-list]])

