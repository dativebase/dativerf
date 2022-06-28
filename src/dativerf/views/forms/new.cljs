(ns dativerf.views.forms.new
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require [clojure.string :as str]
            [dativerf.events :as events]
            [dativerf.fsms.new-form :as new-form-fsm]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [reagent.ratom :as r]
            [re-frame.core :as re-frame]
            [re-com.box :refer [flex-child-style align-style]]
            [re-com.core :as re-com]))

(def statuses
  (mapv (fn [x] {:id x :name x})
        ["tested" "requires testing"]))

(defn- key-up-input [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch
     [::events/user-clicked-create-new-form-button])))

(def model-metadata
  {:new-form/narrow-phonetic-transcription
   {:label "narr. phon. transcr."
    :tooltip "A narrow phonetic transcription, probably in IPA."}
   :new-form/phonetic-transcription
   {:label "phon. transcr."
    :tooltip "A phonetic transcription, probably in IPA."}
   :new-form/transcription
   {:tooltip "A transcription, probably orthographic."}
   :new-form/morpheme-break
   {:tooltip "A sequence of morpheme shapes and delimiters. The OLD assumes
              phonemic shapes (e.g., “in-perfect”), but phonetic (i.e.,
              allomorphic, e.g., “im-perfect”) ones are ok."}
   :new-form/morpheme-gloss
   {:tooltip "A sequence of morpheme glosses and delimiters, isomorphic to
             the morpheme break sequence, e.g., “NEG-parfait”."}
   :new-form/translations
   {:tooltip "One or more translations for the form. Each translation may have
              its own grammaticality/acceptibility specification."}
   :new-form/comments
   {:tooltip "General-purpose field for notes and commentary about the form."}
   :new-form/speaker-comments
   {:tooltip "Field specifically for comments about the form made by the
              speaker/consultant."}
   :new-form/elicitation-method
   {:tooltip "How the form was elicited. Examples: “volunteered”, “judged
              elicitor’s utterance”, “translation task”, etc."}
   :new-form/tags
   {:tooltip "Tags for categorizing your forms."}
   :new-form/syntactic-category
   {:tooltip "The category (syntactic and/or morphological) of the form."}
   :new-form/date-elicited
   {:tooltip "The date this form was elicited"}
   :new-form/speaker
   {:tooltip "The speaker (consultant) who produced or judged the form."}
   :new-form/elicitor
   {:tooltip "The linguistic fieldworker who elicited the form with the help
              of the consultant."}
   :new-form/verifier
   {:tooltip "The user who has verified the reliability/accuracy of this form."}
   :new-form/source
   {:tooltip "The textual source (e.g., research paper, text collection, book
              of learning materials) from which the form was drawn, if
              applicable."}
   :new-form/files
   {:tooltip "Digital files (e.g., audio, video, image or text) that are
              associated to this form."}
   :new-form/syntax
   {:tooltip "A syntactic phrase structure representation in some kind of
              string-based format."}
   :new-form/semantics
   {:tooltip "A semantic representation of the meaning of the form in some
              string-based format."}
   :new-form/status
   {:tooltip "The status of the form: “tested” for data that have been
              elicited/tested/verified with a consultant or “requires testing”
              for data that are posited and still need testing/elicitation."}})

(defn model-x [x model]
  (or (-> model model-metadata x)
      (-> model name utils/kebab->space)))
(def model-label (partial model-x :label))
(def model-placeholder (partial model-x :placeholder))
(def model-tooltip (partial model-x :tooltip))

(defn label [model]
  (let [showing? (r/atom false)
        tooltip (or (and model (model-tooltip model)) "")]
    [re-com/popover-tooltip
     :label (or (and model (model-tooltip model)) "")
     :showing? showing?
     :width (when (> (count tooltip) 80) "400px")
     :anchor
     [re-com/box
      :class (str (styles/attr-label) " " (styles/objlang))
      :align :end
      :padding "0 1em 0 0"
      :attr {:on-mouse-over (re-com/handler-fn (reset! showing? true))
             :on-mouse-out (re-com/handler-fn (reset! showing? false))}
      :child (or (and model (model-label model)) "")]]))

(defn transcription [grammaticalities]
  (let [invalid-msg
        @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                              :transcription])]
    [re-com/h-box
     :gap "10px"
     :children
     [[label :new-form/transcription]
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
       :status (and invalid-msg :error)
       :status-icon? invalid-msg
       :status-tooltip invalid-msg
       :attr {:auto-focus true
              :on-key-up key-up-input}
       :on-change
       (fn [transcription] (re-frame/dispatch-sync
                            [::events/user-changed-new-form-transcription
                             transcription]))]]]))

(defn- fire-on-enter-space [event-vec]
  (re-com/handler-fn
   (when (some #{(.-key event)} ["Enter" " "])
     (re-frame/dispatch event-vec)
     (.preventDefault event))))

(defn- add-new-translation-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-plus"
   :size :smaller
   :tooltip "add another translation"
   :attr {:tab-index "0"
          :on-key-down
          (fire-on-enter-space
           [::events/user-clicked-add-new-translation-button])}
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-add-new-translation-button]))])

(defn- remove-translation-button [index]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-minus"
   :size :smaller
   :tooltip "remove this translation"
   :attr {:tab-index "0"
          :on-key-down
          (fire-on-enter-space
           [::events/user-clicked-remove-translation-button index])}
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-remove-translation-button index]))])

(defn translation [index grammaticalities]
  (let [invalid-msg
        @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                              :translations])]
    [re-com/h-box
     :gap "10px"
     :children
     [[label (when (zero? index) :new-form/translations)]
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
       :status (and invalid-msg :error)
       :status-icon? invalid-msg
       :status-tooltip invalid-msg
       :attr {:on-key-up key-up-input}
       :on-change
       (fn [transcription]
         (re-frame/dispatch-sync
          [::events/user-changed-new-form-translation-transcription
           index transcription]))]
      [re-com/box
       :class (styles/default)
       :child
       (if (zero? index)
         [add-new-translation-button]
         [remove-translation-button index])]]]))

(defn translations [grammaticalities]
  [re-com/v-box
   :gap "10px"
   :children
   (for [index (range (count @(re-frame/subscribe [:new-form/translations])))]
     ^{:key (str "translation-" index)}
     [translation index grammaticalities])])

(defn labeled-input [model input-widget]
  [re-com/h-box
   :gap "10px"
   :children
   [[label model]
    input-widget]])

(defn text-input [model event]
  (when (some #{(utils/set-kw-ns-to-form model)}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [field (utils/remove-namespace model)
          invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                field])]
      [re-com/h-box
       :gap "10px"
       :children
       [[label model]
        [re-com/input-text
         :change-on-blur? false
         :placeholder (model-placeholder model)
         :width "560px"
         :model @(re-frame/subscribe [model])
         :status (and invalid-msg :error)
         :status-icon? invalid-msg
         :status-tooltip invalid-msg
         :attr {:on-key-up key-up-input}
         :on-change
         (fn [val] (re-frame/dispatch-sync [event val]))]]])))

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

(defn invalid-warning []
  (when (= ::new-form-fsm/invalid @(re-frame/subscribe [:new-form/state]))
    [re-com/alert-box
     :alert-type :danger
     :heading "Invalid Form"
     :body @(re-frame/subscribe [:new-form/general-validation-error-message])]))

(defn inputs [{:keys [grammaticalities]}]
  [re-com/v-box
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[text-input :new-form/narrow-phonetic-transcription
     ::events/user-changed-new-form-narrow-phonetic-transcription]
    [text-input :new-form/phonetic-transcription
     ::events/user-changed-new-form-phonetic-transcription]
    [transcription grammaticalities]
    [text-input :new-form/morpheme-break
     ::events/user-changed-new-form-morpheme-break]
    [text-input :new-form/morpheme-gloss
     ::events/user-changed-new-form-morpheme-gloss]
    [translations grammaticalities]]])

;; This is copy/modified from the re-com source for input-text. See
;; https://github.com/day8/re-com/blob/master/src/re_com/input_text.cljs#L162-L201
(defn field-invalid-warning [message]
  (let [showing? (r/atom false)]
    [re-com/popover-tooltip
     :label message
     :position :right-center
     :status :error
     :showing? showing?
     :anchor
     [:i {:class "zmdi zmdi-hc-fw zmdi-alert-circle zmdi-spinner form-control-feedback"
          :style {:position "static"
                  :height "auto"
                  :color "#d50000"
                  :opacity "1"}
          :on-mouse-over (re-com/handler-fn (reset! showing? true))
          :on-mouse-out  (re-com/handler-fn (reset! showing? false))}]
     :style (merge (flex-child-style "none")
                   (align-style :align-self :center)
                   {:font-size   "130%"
                    :margin-left "4px"})]))

(defn named-resource-single-select
  ([choices model event]
   (named-resource-single-select choices model event :name))
  ([choices model event label-fn]
   (named-resource-single-select choices model event label-fn false))
  ([choices model event label-fn filter-box?]
   (when (some #{(utils/set-kw-ns-to-form model)}
               @(re-frame/subscribe [::subs/visible-form-fields]))
     (let [field (utils/remove-namespace model)
           invalid-msg
           @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                 field])]
       [labeled-input
        model
        [re-com/h-box
         :children
         [[re-com/single-dropdown
           :choices (sort-by (comp str/lower-case :label)
                             (for [r choices] {:id (:id r) :label (label-fn r)}))
           :width (if invalid-msg "525px" "560px")
           :filter-box? filter-box?
           :model @(re-frame/subscribe [model])
           ;; This style is hacky, but it's the best I could do
           :style (when invalid-msg {:border "1px solid #d50000"
                                     :border-radius "5px"})
           :on-change (fn [value] (re-frame/dispatch [event value]))]
          (when invalid-msg [field-invalid-warning invalid-msg])]]]))))

(defn tags [available-tags]
  (when (some #{:form/tags} @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                :tags])]
      [labeled-input
       :new-form/tags
       [re-com/h-box
        :children
        [[re-com/selection-list
          :choices (sort-by :label
                            (for [tag available-tags]
                              {:id (:id tag) :label (:name tag)}))
          :width (if invalid-msg "523px" "558px")
          :height "60px"
          :model @(re-frame/subscribe [:new-form/tags])
          :on-change (fn [tags]
                       (re-frame/dispatch
                        [::events/user-changed-new-form-tags tags]))]
         (when invalid-msg [field-invalid-warning invalid-msg])]]])))

(defn date-elicited []
  (when (some #{:form/date-elicited}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                :date-elicited])]
      [re-com/h-box
       :class (styles/default)
       :gap "10px"
       :children
       [[label :new-form/date-elicited]
        ;; TODO: the datepicker of re-com doesn't work that well. For instance, the
        ;; :show-today? attribute doesn't work. This seems to be a known issue.
        [re-com/datepicker-dropdown
         :model @(re-frame/subscribe [:new-form/date-elicited])
         :show-today? true
         :on-change (fn [date-elicited]
                      (re-frame/dispatch
                       [::events/user-changed-new-form-date-elicited
                        date-elicited]))]
        [re-com/md-circle-icon-button
         :md-icon-name "zmdi-delete"
         :size :smaller
         :tooltip "remove date elicited"
         :on-click
         (fn [_]
           (re-frame/dispatch [::events/user-changed-new-form-date-elicited nil]))]
        (when invalid-msg [field-invalid-warning invalid-msg])]])))

(defn- person->name [person]
  (str (:first-name person) " " (:last-name person)))

(defn- source->citation [{:keys [author year]}]
  (str author " (" year ")"))

(defn secondary-inputs [{:keys [elicitation-methods sources speakers
                                syntactic-categories users]
                         available-tags :tags}]
  (when @(re-frame/subscribe [::subs/forms-new-form-secondary-fields-visible?])
    [re-com/v-box
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[text-input :new-form/comments ::events/user-changed-new-form-comments]
      [text-input :new-form/speaker-comments
       ::events/user-changed-new-form-speaker-comments]
      [named-resource-single-select elicitation-methods
       :new-form/elicitation-method
       ::events/user-changed-new-form-elicitation-method]
      [tags available-tags]
      [named-resource-single-select syntactic-categories
       :new-form/syntactic-category
       ::events/user-changed-new-form-syntactic-category]
      [date-elicited]
      [named-resource-single-select speakers :new-form/speaker
       ::events/user-changed-new-form-speaker person->name]
      [named-resource-single-select users :new-form/elicitor
       ::events/user-changed-new-form-elicitor person->name]
      [named-resource-single-select users :new-form/verifier
       ::events/user-changed-new-form-verifier person->name]
      [named-resource-single-select sources :new-form/source
       ::events/user-changed-new-form-source source->citation true]
      ;; TODO: files select. The original Dative has a custom search interface
      ;; that hits the POST /files/search endpoint to return the selectable
      ;; options.
      [text-input :new-form/syntax ::events/user-changed-new-form-syntax]
      [text-input :new-form/semantics
       ::events/user-changed-new-form-semantics]
      [named-resource-single-select statuses
       :new-form/status ::events/user-changed-new-form-status]]]))

(defn save-button []
  [re-com/box
   :child
   [re-com/button
    :label "Save"
    :tooltip "create this new form"
    :disabled? (not= :dativerf.fsms.new-form/ready
                     @(re-frame/subscribe [:new-form/state]))
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
       :class (styles/form-sub-interface)
       :children
       [[header]
        [invalid-warning]
        [inputs forms-new-data]
        [secondary-inputs forms-new-data]
        [footer]]]
      (do (re-frame/dispatch [::events/fetch-new-form-data])
          [re-com/throbber :size :large]))))
