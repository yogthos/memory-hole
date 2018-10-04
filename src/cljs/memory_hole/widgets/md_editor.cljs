(ns memory-hole.widgets.md-editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(def ^{:private true} hint-limit 10)

(def ^{:private true} word-pattern-left #".*(#.*)$")

(def ^{:private true} word-pattern-right #"^(#?.*).*")

(defn- current-word [cm]
  "Returns current word and its coordinates from editor cm."
  (let [cursor (.getCursor cm "from")
        line   (.-line cursor)
        char   (.-ch cursor)
        text   (.getLine (.-doc cm) line)
        parts  (split-at char text)
        leftp  (->> (first parts) (apply str) (re-matches word-pattern-left) second)
        rightp (->> (second parts) (apply str) (re-matches word-pattern-right) second)]
    {:word (str leftp rightp)
     :from (- char (count leftp))
     :to   (+ char (count rightp))}))

(defn- apply-hint [editor self data]
  "Applies hint data to editor by replacing its sub-part."
  (let [cursor (.getCursor editor "from")
        pos  (current-word editor)
        from (:from pos)
        to   (:to pos)
        word (:word pos)]
    (.replaceRange editor
                   (str "[#" (.-text data) "](/issue/" (.-text data) ")")
                   (clj->js {:line (.-line cursor)
                             :ch   from})
                   (clj->js {:line (.-line cursor)
                             :ch   to}))))

(defn- render-hint [element self data]
  "Renders hint data into element."
  (r/render
   [:div.list-group-item
    [:span "#" (get-in (js->clj data) ["displayText" "support-issue-id"])]
    " "
    [:span (get-in (js->clj data) ["displayText" "title"])]]
   element))

(defn- create-hint [hint]
  "Creates hint for show-hint extension."
  {:text        (str (:support-issue-id hint))
   :displayText hint
   :render      render-hint
   :hint        apply-hint})

(defn- markdown-hints [hints]
  "Show-hint handler."
  (fn [c o]
    (clj->js {:list (map create-hint hints)
              :from (.getCursor c "from")})))

(defn- show-hint [cm]
  "Shows hints for editor cm via show-hint extension."
  (fn [k r os ns]
    (.showHint
     cm
     (clj->js {:hint (markdown-hints (:issues @ns))
               :completeSingle false}))))

(defn- sent-hint-request [cm]
  "Sends hint request to server."
  (let [current (:word (current-word cm))]
    (if (not (empty? current))
      (dispatch [:get-issue-hints (subs current 1) hint-limit]))))

(defn- editor-set-shortcut [editor]
  "Sets shortcut for issue hints into editor."
  (aset
   editor
   "options"
   "extraKeys"
   (clj->js
    {"Ctrl-Space" sent-hint-request})))

(extend-type js/NodeList
  IIndexed
  (-nth
    ([array n]
     (if (< n (alength array)) (aget array n)))
    ([array n not-found]
     (if (< n (alength array)) (aget array n)
         not-found))))

(defn- inject-editor-implementation
  "Injects CodeMirror editor instance into SimpleMDE, which can be then
  extended by plugins. This is a hack and can be removed if SimpleMDE
  changes availability of CodeMirror."
  [editor]
  (do
    ;; move editor into text area
    (-> editor .-codemirror .toTextArea)
    ;; create new instance via fromTextArea (recommended)
    (aset
     editor
     "codemirror"
     (.fromTextArea js/CodeMirror (-> editor .-codemirror .getTextArea)))
    ;; manipulate DOM so element is on right place
    (.insertBefore (-> editor
                       .-codemirror
                       .getScrollerElement
                       .-parentNode
                       .-parentNode)
                   (-> editor
                       .-codemirror
                       .getScrollerElement
                       .-parentNode)
                   (-> editor
                       .-codemirror
                       .getScrollerElement
                       .-parentNode
                       .-parentNode
                       .-childNodes
                       (nth 3)))))

(defn editor [text issue-hints]
  (r/create-class
   {:component-did-mount
    #(let [editor      (js/SimpleMDE.
                        (clj->js
                         {:autofocus    true
                          :spellChecker false
                          :status       false
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
                          :initialValue @text}))
           hints-shown (atom false)]
       (do
         (inject-editor-implementation editor)
         (editor-set-shortcut (-> editor .-codemirror))
         (add-watch issue-hints :watch-issue-hints (show-hint (-> editor .-codemirror)))
         (-> editor .-codemirror (.on "change" (fn [] (reset! text (.value editor)))))
         (-> editor .-codemirror (.on "change" (fn [] (when @hints-shown (sent-hint-request (-> editor .-codemirror))))))
         (-> editor .-codemirror (.on "startCompletion" (fn [] (reset! hints-shown true))))
         (-> editor .-codemirror (.on "endCompletion" (fn [] (reset! hints-shown false))))))
    :reagent-render
    (fn [text issue-hints]
      (do
        @issue-hints ;; dereference hints, so add-watch is not optimized out
        [:textarea]))}))
