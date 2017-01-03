(ns memory-hole.widgets.lazy-scroll
  (:require
    [reagent.core :as r]))

(defn- get-scroll-top []
  (if (exists? (.-pageYOffset js/window))
    (.-pageYOffset js/window)
    (.-scrollTop (or (.-documentElement js/document)
                     (.-parentNode (.-body js/document))
                     (.-body js/document)))))

(defn- node-top-position [node]
  (if node
    (+ (.-offsetTop node)
       (node-top-position (.-offsetParent node)))
    0))

(defn debounce
  "wraps f with a function that
  only calls it after a threshold
  without new calls has passed."
  [f threshold]
  (let [t (atom nil)]
    (fn [& args]
      (swap! t (fn [t]
                 (when t (js/clearTimeout t))
                 (js/setTimeout #(apply f args) threshold))))))

(defn load-more? [node threshold]
  (< (- (+ (node-top-position node) (.-offsetHeight node))
        (get-scroll-top)
        (.-innerHeight js/window))
     threshold))

(defn lazy-scroll [_]
  (let [listener-fn            (atom nil)
        detach-scroll-listener (fn []
                                 (when @listener-fn
                                   (.removeEventListener js/window "scroll" @listener-fn)
                                   (.removeEventListener js/window "resize" @listener-fn)
                                   (reset! listener-fn nil)))
        scroll-listener        (debounce
                                 (fn [this]
                                   (let [{:keys [load-fn threshold]} (r/props this)]
                                     (when (load-more? (r/dom-node this) threshold)
                                       (detach-scroll-listener)
                                       (load-fn))))
                                 200)
        attach-scroll-listener (fn [this]
                                 (let [{:keys [can-show-more?]} (r/props this)]
                                   (when can-show-more?
                                     (when-not @listener-fn
                                       (reset! listener-fn (partial scroll-listener this))
                                       (.addEventListener js/window "scroll" @listener-fn)
                                       (.addEventListener js/window "resize" @listener-fn))
                                     (scroll-listener this))))]
    (r/create-class
      {:component-did-mount attach-scroll-listener
       :component-did-update attach-scroll-listener
       :component-will-unmount detach-scroll-listener
       :reagent-render (fn [_] [:div])})))
