(ns memory-hole.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [memory-hole.bootstrap :as bs]
            [memory-hole.pages.home :refer [home-page]]
            [memory-hole.pages.issues :refer [edit-issue-page view-issue-page]]
            [memory-hole.pages.auth :refer [login-page logout]]))

(defn loading-throbber
  []
  (let [loading? (subscribe [:loading?])]
    (when @loading?
      [bs/Modal
       {:show true}
       [bs/Modal.Body
        [:div.spinner
         [:div.bounce1]
         [:div.bounce2]
         [:div.bounce3]]]])))

#_(defn nav-link [uri title page]
  (let [active-page (subscribe [:active-page])]
    [bs/NavItem {:href uri :active (= page @active-page)} title]))

(defn navbar [user]
  [bs/Navbar {:inverse true}
   [bs/Navbar.Header]
   [bs/Navbar.Brand
    [:a#logo {:href "#/"}
     [:span "Issues"]]]
   [bs/Navbar.Collapse
    #_[bs/Nav
     [nav-link "#/" "Home" :home]]
    [bs/Nav {:pull-right true}
     [bs/MenuItem {:on-click logout}
      (r/as-component [:span "Logout " (:display-name user)])]]]])

(defmulti pages identity)
(defmethod pages :home [] [home-page])
(defmethod pages :login [] [login-page])
(defmethod pages :edit-issue [] (.scrollTo js/window 0 0) [edit-issue-page])
(defmethod pages :view-issue [] (.scrollTo js/window 0 0) [view-issue-page])
(defmethod pages :default [] [:div])

(defn main-page []
  (r/with-let [active-page (subscribe [:active-page])
               user        (subscribe [:user])]
    (if @user
      [:div
       [navbar @user]
       [loading-throbber]
       [:div.container
        (pages @active-page)]]
      (pages :login))))
