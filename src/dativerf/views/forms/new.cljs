(ns dativerf.views.forms.new
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require [clojure.string :as str]
            [dativerf.events :as events]
            [dativerf.fsms.new-form :as new-form-fsm]
            [dativerf.models.form :as model]
            [dativerf.models.utils :as mutils]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com]))

;; Utils

(defn- person->name [person]
  (str (:first-name person) " " (:last-name person)))

(defn- source->citation [{:keys [author year]}]
  (str author " (" year ")"))

(def ^:private models->events
  "Map from model (i.e., subscription) keyword (with :edited-settings ns) to
  event keyword (with :dativerf.events ns)."
  (->> model/editable-keys
       (map (fn [k]
              [(keyword "new-form" k)
               (keyword "dativerf.events"
                        (str "user-changed-new-form-" (name k)))]))
       (into {})))

(defn- field-event [model]
  [(utils/remove-namespace model)
   (models->events model)])

(def ^:private statuses
  (mapv (fn [x] {:id x :name x}) ["tested" "requires testing"]))

(def ^:private field->choices
  {:elicitation-method ::subs/mini-elicitation-methods
   :tags ::subs/mini-tags
   :syntactic-category ::subs/mini-syntactic-categories
   :speaker ::subs/mini-speakers
   :elicitor ::subs/mini-users
   :verifier ::subs/mini-users
   :source ::subs/mini-sources
   :status statuses})

(def ^:private field->label-fn
  {:speaker person->name
   :elicitor person->name
   :verifier person->name
   :source source->citation})

(defn- key-up-input [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch
     [::events/user-clicked-create-new-form-button])))

(def ^:private label-str (partial mutils/label-str model/metadata))
(def ^:private placeholder (partial mutils/placeholder model/metadata))
(def ^:private description (partial mutils/description model/metadata))

(defn- fire-on-enter-space [event-vec]
  (re-com/handler-fn
   (when (some #{(.-key event)} ["Enter" " "])
     (re-frame/dispatch event-vec)
     (.preventDefault event))))

(def ^:private label (partial widgets/label-with-tooltip
                              model/metadata
                              styles/attr-label))

(def ^:private labeled-el (partial widgets/labeled-el model/metadata styles/attr-label))

(defn- invalid-warning []
  (when (= ::new-form-fsm/invalid @(re-frame/subscribe [:new-form/state]))
    [re-com/alert-box
     :alert-type :danger
     :heading "Invalid Form"
     :body @(re-frame/subscribe [:new-form/general-validation-error-message])]))

;; Buttons

(defn- save-button []
  [re-com/box
   :child
   [re-com/button
    :label "Save"
    :tooltip "create this new form"
    :disabled? (not= :dativerf.fsms.new-form/ready
                     @(re-frame/subscribe [:new-form/state]))
    :on-click (fn [_e] (re-frame/dispatch
                        [::events/user-clicked-create-new-form-button]))]])

(defn- toggle-secondary-inputs-button []
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
   (fn [_] (re-frame/dispatch
            [::events/user-clicked-toggle-secondary-new-form-fields]))])

(defn- hide-new-form-interface-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-close"
   :size :smaller
   :tooltip "hide new form interface"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-new-form-button]))])

(defn- reset-new-form-values-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-delete"
   :size :smaller
   :tooltip "clear this form: reset all fields to their default values"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-clear-new-form-interface]))])

(defn- help-creating-form-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-help"
   :size :smaller
   :tooltip "help with creating a new form"
   :disabled? true
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-help-creating-new-form]))])

(defn- reset-date-elicited-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-delete"
   :size :smaller
   :tooltip "remove date elicited"
   :on-click
   (fn [_]
     (re-frame/dispatch [::events/user-changed-new-form-date-elicited nil]))])

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

;; Inputs

(defn- text-input [model]
  (when (some #{(utils/set-kw-ns-to-form model)}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [[field event] (field-event model)
          invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                field])]
      [labeled-el
       field
       [re-com/input-text
        :change-on-blur? false
        :placeholder (placeholder field)
        :width "560px"
        :model @(re-frame/subscribe [model])
        :status (and invalid-msg :error)
        :status-icon? invalid-msg
        :status-tooltip invalid-msg
        :attr {:on-key-up key-up-input}
        :on-change
        (fn [val] (re-frame/dispatch-sync [event val]))]])))

(defn- named-resource-single-select
  ([model] (named-resource-single-select model {}))
  ([model {:keys [filter-box? allow-empty?] :or {filter-box? false
                                                 allow-empty? true}}]
   (when (some #{(utils/set-kw-ns-to-form model)}
               @(re-frame/subscribe [::subs/visible-form-fields]))
     (let [[field event] (field-event model)
           label-fn (field->label-fn field :name)
           choices (let [tmp (field->choices field)]
                     (if (keyword? tmp)
                       @(re-frame/subscribe [tmp])
                       tmp))
           invalid-msg
           @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                 field])]
       [labeled-el
        field
        [re-com/h-box
         :children
         [[re-com/single-dropdown
           :choices (concat (if allow-empty? [{:id nil :label "\u2205"}] [])
                            (sort-by (comp str/lower-case :label)
                                     (for [r choices] {:id (:id r) :label (label-fn r)})))
           :width (if invalid-msg "525px" "560px")
           :filter-box? filter-box?
           :model @(re-frame/subscribe [model])
           ;; This style is hacky, but it's the best I could do
           :style (when invalid-msg {:border "1px solid #d50000"
                                     :border-radius "5px"})
           :on-change (fn [value] (re-frame/dispatch [event value]))]
          (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]]))))

(defn- grammaticality []
  [re-com/single-dropdown
   :choices (for [g (distinct @(re-frame/subscribe [::subs/grammaticalities]))]
              ^{:key (str "transcription-grammaticality-" g)}
              {:id g :label g})
   :width "50px"
   :model @(re-frame/subscribe [:new-form/grammaticality])
   :on-change (fn [grammaticality]
                (re-frame/dispatch
                 [::events/user-changed-new-form-grammaticality
                  grammaticality]))])

(defn- transcription []
  (let [invalid-msg @(re-frame/subscribe
                      [:new-form/field-specific-validation-error-message
                       :transcription])]
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
                           transcription]))]))

(defn- grammaticality-transcription []
  [labeled-el
   :transcription
   [re-com/h-box :gap "10px" :children [[grammaticality] [transcription]]]])

(defn- translation-grammaticality [index]
  [re-com/single-dropdown
   :choices (for [g (distinct @(re-frame/subscribe [::subs/grammaticalities]))]
              ^{:key (str "translation-" index "-grammaticality-" g)}
              {:id g :label g})
   :width "50px"
   :model @(re-frame/subscribe [:new-form/translation-grammaticality index])
   :on-change (fn [grammaticality]
                (re-frame/dispatch
                 [::events/user-changed-new-form-translation-grammaticality
                  index grammaticality]))])

(defn- translation-transcription [index]
  (let [invalid-msg @(re-frame/subscribe
                      [:new-form/field-specific-validation-error-message
                       :translations])]
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
         index transcription]))]))

(defn- add-remove-translation-button [index]
  [re-com/box
   :class (styles/default)
   :child (if (zero? index)
            [add-new-translation-button]
            [remove-translation-button index])])

(defn- grammaticality-translation [index]
  [labeled-el
   (when (zero? index) :new-form/translations)
   [re-com/h-box
    :gap "10px"
    :children
    [[translation-grammaticality index]
     [translation-transcription index]
     [add-remove-translation-button index]]]])

(defn- translations []
  [re-com/v-box
   :gap "10px"
   :children
   (for [index (range (count @(re-frame/subscribe [:new-form/translations])))]
     ^{:key (str "translation-" index)}
     [grammaticality-translation index])])

(defn- tags []
  (when (some #{:form/tags} @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [available-tags @(re-frame/subscribe [::subs/mini-tags])
          invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                :tags])]
      [labeled-el
       :tags
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
         (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]])))

(defn- date-elicited []
  (when (some #{:form/date-elicited}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [invalid-msg
          @(re-frame/subscribe [:new-form/field-specific-validation-error-message
                                :date-elicited])]
      [re-com/h-box
       :class (styles/default)
       :gap "10px"
       :children
       [[label :date-elicited]
        ;; TODO: the datepicker of re-com doesn't work that well. For instance, the
        ;; :show-today? attribute doesn't work. This seems to be a known issue.
        [re-com/datepicker-dropdown
         :model @(re-frame/subscribe [:new-form/date-elicited])
         :show-today? true
         :on-change (fn [date-elicited]
                      (re-frame/dispatch
                       [::events/user-changed-new-form-date-elicited
                        date-elicited]))]
        [reset-date-elicited-button]
        (when invalid-msg [widgets/field-invalid-warning invalid-msg])]])))

(defn- inputs []
  [re-com/v-box
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[text-input :new-form/narrow-phonetic-transcription]
    [text-input :new-form/phonetic-transcription]
    [grammaticality-transcription]
    [text-input :new-form/morpheme-break]
    [text-input :new-form/morpheme-gloss]
    [translations]]])

(defn- secondary-inputs []
  (when @(re-frame/subscribe [::subs/forms-new-form-secondary-fields-visible?])
    [re-com/v-box
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[text-input :new-form/comments]
      [text-input :new-form/speaker-comments]
      [named-resource-single-select :new-form/elicitation-method]
      [tags]
      [named-resource-single-select :new-form/syntactic-category]
      [date-elicited]
      [named-resource-single-select :new-form/speaker]
      [named-resource-single-select :new-form/elicitor]
      [named-resource-single-select :new-form/verifier]
      [named-resource-single-select :new-form/source {:filter-box? true}]
      ;; TODO: files select. The original Dative has a custom search interface
      ;; that hits the POST /files/search endpoint to return the selectable
      ;; options.
      [text-input :new-form/syntax]
      [text-input :new-form/semantics]
      [named-resource-single-select :new-form/status {:allow-empty? false}]]]))

;; Headers and Footers

(defn header []
  (widgets/header
   {:left [[hide-new-form-interface-button]
           [toggle-secondary-inputs-button]]
    :center [[re-com/box :child "New Form"]]
    :right [[reset-new-form-values-button]
            [help-creating-form-button]]}))

(defn footer []
  (widgets/footer
   {:center [[save-button]
             [toggle-secondary-inputs-button]]}))

(defn interface []
  (when @(re-frame/subscribe [::subs/forms-new-form-interface-visible?])
    (re-frame/dispatch [::events/fetch-new-form-data]) ;; caching makes this often a no-op
    [re-com/v-box
     :gap "10px"
     :class (styles/form-sub-interface)
     :children
     [[header]
      [invalid-warning]
      [inputs]
      [secondary-inputs]
      [footer]]]))
