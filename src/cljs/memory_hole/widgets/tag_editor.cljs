(ns memory-hole.widgets.tag-editor
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe]]
            [memory-hole.bootstrap :as bs]))

(def key-up 38)
(def key-down 40)
(def key-enter 13)
(def key-escape 27)
(def key-backspace 8)

(defn- close-typeahead [selected-index typeahead-hidden? mouse-on-list?]
  (reset! selected-index -1)
  (reset! typeahead-hidden? true)
  (reset! mouse-on-list? false))

(defn- scroll-target-list [event idx]
  (when (> idx -1)
    (let [ul (-> event .-target .-nextSibling)]
      (when-let [element (aget (.getElementsByTagName ul "li") idx)]
        (set! (.-scrollTop ul) (.-offsetTop element))))))

(defn- filter-selections [selections all-tags issue-tags user-input]
  (->> (difference all-tags issue-tags)
       (sort)
       (filter #(s/includes? (s/lower-case %) (some-> user-input s/lower-case)))
       (reset! selections)))

(defn- remove-tag [tags tag]
  (->> tags
       (remove #(= % tag))
       vec))

(defn- add-tag-and-close-typeahead
  [tags new-tag selected-index typeahead-hidden? mouse-on-list?]
  (when (and @new-tag (seq (s/trim @new-tag)))
    (swap! tags conj @new-tag)
    (reset! new-tag nil)
    (close-typeahead selected-index typeahead-hidden? mouse-on-list?)))

(defn- typeahead-item [idx selected-index issue-tags user-input item typeahead-hidden? mouse-on-list?]
  [:li.clickable
   {:tab-index     idx
    :key           idx
    :class         (when (= @selected-index idx) "selected")
    :on-mouse-over #(reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex")))
    :on-click      #(do
                     (reset! user-input item)
                     (add-tag-and-close-typeahead issue-tags user-input selected-index typeahead-hidden? mouse-on-list?))}
   item])

(defn- typeahead-list [issue-tags selections selected-index user-input typeahead-hidden? mouse-on-list?]
  [:ul.dropdown-menu
   {:style          {:display (if (or (empty? @selections) @typeahead-hidden?) :none :block)}
    :on-mouse-enter #(reset! mouse-on-list? true)
    :on-mouse-leave #(reset! mouse-on-list? false)}
   (for [[idx item] (map-indexed vector @selections)]
     ^{:key idx}
     [typeahead-item idx selected-index issue-tags user-input item typeahead-hidden? mouse-on-list?])])

(defn- tag-input [issue-tags]
  (r/with-let
    [all-tags          (->> @(subscribe [:tags])
                            (map :tag)
                            (set))
     typeahead-hidden? (r/atom true)
     mouse-on-list?    (r/atom false)
     selected-index    (r/atom -1)
     selections        (r/atom [])
     user-input        (r/atom nil)
     choose-selected   #(do
                         (reset! user-input (get (vec @selections) @selected-index))
                         (add-tag-and-close-typeahead issue-tags user-input selected-index typeahead-hidden? mouse-on-list?))]
    [:div
     [:div.tags-input
      {:on-click #(some-> % .-target .-lastChild .-firstChild .focus)}
      (for [tag @issue-tags]
        ^{:key tag}
        [:span.tag.label.label-info
         tag
         [:span {:data-role "remove"
                 :on-click  #(swap! issue-tags remove-tag tag)}]])
      [:span
       {:class (if @typeahead-hidden? "dropdown" "dropdown open")}
       [:input
        {:type        :text

         :placeholder "Type tag and press enter to add"

         :value       @user-input

         :on-focus    #(do
                        (reset! typeahead-hidden? false)
                        (if (seq @user-input)
                          (filter-selections selections all-tags (set @issue-tags) @user-input)
                          (reset! selections [])))
         :on-change   #(do
                        (reset! user-input (-> % .-target .-value))
                        (filter-selections selections all-tags (set @issue-tags) @user-input)
                        (reset! typeahead-hidden? false)
                        (reset! selected-index -1))

         :on-blur     #(when-not @mouse-on-list?
                        (close-typeahead selected-index typeahead-hidden? mouse-on-list?))

         :on-key-down #(condp = (.-keyCode %)
                        key-up (do
                                 (.preventDefault %)
                                 (when-not (neg? @selected-index)
                                   (swap! selected-index dec)
                                   (scroll-target-list % @selected-index)))
                        key-down (do
                                   (.preventDefault %)
                                   (when-not (= @selected-index (dec (count @selections)))
                                     (swap! selected-index inc)
                                     (scroll-target-list % @selected-index)))
                        key-enter (if (neg? @selected-index)
                                    (add-tag-and-close-typeahead issue-tags user-input selected-index typeahead-hidden? mouse-on-list?)
                                    (choose-selected))
                        key-backspace (if (empty? @user-input) (swap! issue-tags (comp vec butlast)))
                        key-escape (close-typeahead selected-index typeahead-hidden? mouse-on-list?)
                        "Default")}]
       [typeahead-list issue-tags selections selected-index user-input typeahead-hidden? mouse-on-list?]]]

     (when-let [new-tags (-> (set @issue-tags)
                             (difference all-tags)
                             (not-empty))]
       [:div.new-tags-list
        "Creating tags: "
        (for [tag new-tags]
          ^{:key tag}
          [:span.label.label-danger.new-issue-tag tag])])]))

(defn tag-editor [tags]
  [:div.row
   [:div.col-sm-12
    [tag-input tags]]])
