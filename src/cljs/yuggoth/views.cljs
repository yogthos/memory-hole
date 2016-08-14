(ns yuggoth.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [yuggoth.bootstrap :as bs]))

(defn loading-throbber
  []
  (let [loading? (subscribe [:loading?])]
    (when @loading?
      [:div.loading
       [:div.three-quarters-loader "Loading..."]])))

(defn nav-link [uri title page]
  (let [active-page (subscribe [:active-page])]
    [bs/NavItem {:href uri :active (= page @active-page)} title]))

(defn navbar []
  [bs/Navbar {:inverse true}
   [bs/Navbar.Header]
   [bs/Navbar.Brand
    [:a#logo {:href "#/"}
     [:span "Issues"]]]
   [bs/Navbar.Collapse
    [bs/Nav
     [nav-link "#/" "Home" :home]]]])

(defn home-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "TrackIt"]]]])



(defmulti pages identity)
(defmethod pages :home [] [home-page])
(defmethod pages :default [] [:div])

(defn main-page []
  (r/with-let [active-page (subscribe [:active-page])]
    [:div.container
     [navbar]
     [loading-throbber]
     (pages @active-page)]))