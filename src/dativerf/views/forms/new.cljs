(ns dativerf.views.forms.new
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [clojure.string :as str]))

(defn label [label-text]
  [re-com/box
   :class (str (styles/attr-label) " " (styles/objlang))
   :justify :end
   :padding "0 1em 0 0"
   :child label-text])

(defn transcription [grammaticalities]
  [re-com/h-box
   :gap "10px"
   :children
   [[label "transcription"]
    [re-com/single-dropdown
     :choices (for [g grammaticalities] {:id g :label g})
     :width "50px"
     :model @(re-frame/subscribe [:new-form/grammaticality])
     :on-change (fn [grammaticality]
                  (re-frame/dispatch
                   [::events/user-changed-new-form-grammaticality
                    grammaticality]))]
    [re-com/input-text
     :change-on-blur? false
     :placeholder "transcription"
     :width "500px"
     :model @(re-frame/subscribe [:new-form/transcription])
     :on-change
     (fn [transcription] (re-frame/dispatch-sync
                          [::events/user-changed-new-form-transcription
                           transcription]))]]])

(defn translation [index grammaticalities]
  [re-com/h-box
   :gap "10px"
   :children
   [[label (if (zero? index) "translations" "")]
    [re-com/single-dropdown
     :choices (for [g grammaticalities] {:id g :label g})
     :width "50px"
     :model @(re-frame/subscribe [:new-form/translation-grammaticality index])
     :on-change (fn [grammaticality]
                  (re-frame/dispatch
                   [::events/user-changed-new-form-translation-grammaticality
                    index grammaticality]))]
    [re-com/input-text
     :change-on-blur? false
     :placeholder "translation"
     :width "460px"
     :model @(re-frame/subscribe [:new-form/translation-transcription index])
     :on-change
     (fn [transcription]
       (re-frame/dispatch-sync
        [::events/user-changed-new-form-translation-transcription
         index transcription]))]
    [re-com/box
     :class (styles/default)
     :child
     (if (zero? index)
       [re-com/md-circle-icon-button
        :md-icon-name "zmdi-plus"
        :size :smaller
        :tooltip "add another translation"
        :on-click
        (fn [_]
          (re-frame/dispatch
           [::events/user-clicked-add-new-translation-button]))]
       [re-com/md-circle-icon-button
        :md-icon-name "zmdi-minus"
        :size :smaller
        :tooltip "remove this translation"
        :on-click
        (fn [_]
          (re-frame/dispatch
           [::events/user-clicked-remove-translation-button index]))])]]])

(defn translations [grammaticalities]
  [re-com/v-box
   :gap "10px"
   :children
   (for [index (range (count @(re-frame/subscribe [:new-form/translations])))]
     ^{:key (str "translation-" index)}
     [translation index grammaticalities])])

(defn labeled-input [label-text input-widget]
  [re-com/h-box
   :gap "10px"
   :children
   [[label label-text]
    input-widget]])

(defn text-input [label-text model event]
  [re-com/h-box
   :gap "10px"
   :children
   [[label label-text]
    [re-com/input-text
     :change-on-blur? false
     :placeholder label-text
     :width "560px"
     :model @(re-frame/subscribe [model])
     :on-change
     (fn [val] (re-frame/dispatch-sync [event val]))]]])

(defn toggle-secondary-inputs-button []
  [re-com/md-circle-icon-button
   :md-icon-name
   (if @(re-frame/subscribe [::subs/forms-new-form-secondary-fields-visible?])
     "zmdi-chevron-up"
     "zmdi-chevron-down")
   :size :smaller
   :tooltip
   (if @(re-frame/subscribe [::subs/forms-new-form-secondary-fields-visible?])
     "hide the secondary data input fields"
     "show the secondary data input fields")
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-toggle-secondary-new-form-fields]))])

(defn header-left []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :children
   [[re-com/md-circle-icon-button
     :md-icon-name "zmdi-close"
     :size :smaller
     :tooltip "hide new form interface"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-new-form-button]))]
    [toggle-secondary-inputs-button]]])

(defn header-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children [[re-com/box :child "New Form"]]])

(defn header-right []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :end
   :children
   [[re-com/md-circle-icon-button
     :md-icon-name "zmdi-delete"
     :size :smaller
     :tooltip "clear this form: reset all fields to their default values"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-clear-new-form-interface]))]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-help"
     :size :smaller
     :tooltip "help with creating a new form"
     :disabled? true
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-help-creating-new-form]))]]])

(defn header []
  [re-com/h-box
   :gap "5px"
   :children
   [[header-left]
    [header-center]
    [header-right]]])

(defn inputs [{:keys [grammaticalities]}]
  [re-com/v-box
   :class (styles/objlang)
   :gap "10px"
   :children
   [[transcription grammaticalities]
    [text-input "morpheme break" :new-form/morpheme-break
     ::events/user-changed-new-form-morpheme-break]
    [text-input "morpheme gloss" :new-form/morpheme-gloss
     ::events/user-changed-new-form-morpheme-gloss]
    [translations grammaticalities]]])

(defn named-resource-single-select
  ([label-text choices model event]
   (named-resource-single-select label-text choices model event :name))
  ([label-text choices model event label-fn]
   (named-resource-single-select label-text choices model event label-fn false))
  ([label-text choices model event label-fn filter-box?]
   [labeled-input
    label-text
    [re-com/single-dropdown
     :choices (sort-by (comp str/lower-case :label)
                       (for [r choices] {:id (:id r) :label (label-fn r)}))
     :width "560px"
     :filter-box? filter-box?
     :model @(re-frame/subscribe [model])
     :on-change (fn [value] (re-frame/dispatch [event value]))]]))

(defn tags [available-tags]
  [labeled-input
   "tags"
   [re-com/selection-list
    :choices (sort-by :label
                      (for [tag available-tags]
                        {:id (:id tag) :label (:name tag)}))
    :width "560px"
    :height "60px"
    :model @(re-frame/subscribe [:new-form/tags])
    :on-change (fn [tags]
                 (re-frame/dispatch
                  [::events/user-changed-new-form-tags tags]))]])

(defn date-elicited []
  [re-com/h-box
   :class (styles/default)
   :gap "10px"
   :children
   [[label "date elicited"]
    ;; TODO: the datepicker of re-com doesn't work that well. For instance, the
    ;; :show-today? attribute doesn't work. This seems to be a known issue.
    [re-com/datepicker-dropdown
     :model @(re-frame/subscribe [:new-form/date-elicited])
     :show-today? true
     :on-change (fn [date-elicited]
                  (re-frame/dispatch
                   [::events/user-changed-new-form-date-elicited
                    date-elicited]))]]])

(defn- person->name [person]
  (str (:first-name person) " " (:last-name person)))

(defn- source->citation [{:keys [author year]}]
  (str author " (" year ")"))

(defn secondary-inputs [{:keys [elicitation-methods sources speakers
                                syntactic-categories users]
                         available-tags :tags}]
  (when @(re-frame/subscribe [::subs/forms-new-form-secondary-fields-visible?])
    [re-com/v-box
     :class (styles/objlang)
     :gap "10px"
     :children
     [[text-input "comments" :new-form/comments
       ::events/user-changed-new-form-comments]
      [text-input "speaker-comments" :new-form/speaker-comments
       ::events/user-changed-new-form-speaker-comments]
      [named-resource-single-select "elicitation method" elicitation-methods
       :new-form/elicitation-method
       ::events/user-changed-new-form-elicitation-method]
      [tags available-tags]
      [named-resource-single-select "syntactic category" syntactic-categories
       :new-form/syntactic-category
       ::events/user-changed-new-form-syntactic-category]
      [date-elicited]
      [named-resource-single-select "speaker" speakers :new-form/speaker
       ::events/user-changed-new-form-speaker person->name]
      [named-resource-single-select "elicitor" users :new-form/elicitor
       ::events/user-changed-new-form-elicitor person->name]
      [named-resource-single-select "source" sources :new-form/source
       ::events/user-changed-new-form-source source->citation true]
      ;; TODO: files select. The original Dative has a custom search interface
      ;; that hits the POST /files/search endpoint to return the selectable
      ;; options.
      [text-input "syntax" :new-form/syntax
       ::events/user-changed-new-form-syntax]
      [text-input "semantics" :new-form/semantics
       ::events/user-changed-new-form-semantics]]]))

(defn save-button []
  [re-com/box
   :child
   [re-com/button
    :label "Save"
    :tooltip "create this new form"
    :on-click (fn [_e] (re-frame/dispatch
                        [::events/user-clicked-create-new-form-button]))]])

(defn footer-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children
   [[save-button]
    [toggle-secondary-inputs-button]]])

(defn footer []
  [re-com/h-box
   :gap "5px"
   :children
   [[footer-center]]])

(defn interface []
  (when @(re-frame/subscribe [::subs/forms-new-form-interface-visible?])
    (if-let [forms-new-data @(re-frame/subscribe [::subs/forms-new])]
      [re-com/v-box
       :gap "10px"
       :class (styles/new-form-interface)
       :children
       [[header]
        [inputs forms-new-data]
        [secondary-inputs forms-new-data]
        [footer]]]
      (do (re-frame/dispatch [::events/fetch-new-form-data])
          [re-com/throbber :size :large]))))
