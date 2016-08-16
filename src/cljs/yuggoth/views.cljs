(ns yuggoth.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [yuggoth.bootstrap :as bs]
            [yuggoth.pages.home :refer [home-page]]
            [yuggoth.pages.issues :refer [edit-issue-page view-issue-page]]
            [yuggoth.pages.auth :refer [login-page logout]]))

(defn loading-throbber
  []
  (let [loading? (subscribe [:loading?])]
    (when @loading?
      [:div.loading
       [:div.three-quarters-loader "Loading..."]])))

(defn nav-link [uri title page]
  (let [active-page (subscribe [:active-page])]
    [bs/NavItem {:href uri :active (= page @active-page)} title]))

(defn navbar [user]
  [bs/Navbar {:inverse true}
   [bs/Navbar.Header]
   [bs/Navbar.Brand
    [:a#logo {:href "#/"}
     [:span "Issues"]]]
   [bs/Navbar.Collapse
    [bs/Nav
     [nav-link "#/" "Home" :home]]
    (when @user
      [bs/Nav {:pull-right true}
       [bs/MenuItem {:on-click logout} "Logout"]])]])

(defmulti pages identity)
(defmethod pages :home []  #_[view-issue-page] [edit-issue-page] #_[home-page])
(defmethod pages :login [] [login-page])
(defmethod pages :edit-issue [] [edit-issue-page])
(defmethod pages :view-issue [] [view-issue-page])
(defmethod pages :default [] [:div])

(defn main-page []
  (r/with-let [active-page (subscribe [:active-page])
               user        (subscribe [:user])]
    (if @user
      [:div
       [navbar user]
       [loading-throbber]
       [:div.container
        (pages @active-page)]]
      (pages :login))))