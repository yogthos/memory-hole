(ns memory-hole.attachments
  (:require [goog.events :as gev]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch]]
            [memory-hole.bootstrap :as bs])
  (:import goog.net.IframeIo
           goog.net.EventType
           [goog.events EventType]))

(defn upload-file! [upload-form-id on-success on-error]
  (let [io (IframeIo.)]
    (gev/listen
      io goog.net.EventType.SUCCESS
      #(on-success (.getResponseJson io)))
    (gev/listen
      io goog.net.EventType.ERROR
      on-error)
    (.setErrorChecker io #(.isSuccess io))
    (.sendFromForm
      io
      (.getElementById js/document upload-form-id)
      "/api/attach-file")))

(defn upload-form [support-issue-id modal-open? success-action]
  (r/with-let [form-id    "upload-form"
               uploading? (r/atom false)
               on-success (fn [filename]
                            (reset! uploading? false)
                            (success-action filename))
               on-error   (fn []
                            (reset! uploading? false)
                            (dispatch [:set-error "failed to upload the file"]))]
    [bs/Modal
     {:show @modal-open?}
     [bs/Modal.Header
      [bs/Modal.Title "Upload File"]]
     [bs/Modal.Body
      (if @uploading?
        [:div.spinner
         [:div.bounce1]
         [:div.bounce2]
         [:div.bounce3]]
        [:form {:id       form-id
                :enc-type "multipart/form-data"
                :method   "POST"}
         [:fieldset.form-group
          [:label {:for "file"} "select a file to upload"]
          [:input {:type "hidden" :name "support-issue-id"
                   :value (str support-issue-id)}]
          [:input.form-control {:id "file" :name "file" :type "file"}]]])]
     [bs/Modal.Footer
      [:div.pull-right
       [bs/Button
        {:bs-style "danger"
         :disabled @uploading?
         :on-click #(reset! modal-open? false)}
        "Cancel"]
       [bs/Button
        {:bs-style "primary"
         :disabled @uploading?
         :on-click #(do
                     (reset! uploading? true)
                     (upload-file! form-id on-success on-error))}
        "Upload"]]]]))
