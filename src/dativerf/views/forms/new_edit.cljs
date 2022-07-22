(ns dativerf.views.forms.new-edit
  "This namespace defines an interface that is designed to work for both
  creating a new form and editing an existing one."
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require [clojure.string :as str]
            [dativerf.events :as events]
            [dativerf.fsms.edit-form :as edit-form-fsm]
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

(defn- edit? [new-or-edit] (= :edit new-or-edit))

(defn- person->name [person]
  (str (:first-name person) " " (:last-name person)))

(defn- source->citation [{:keys [author year]}]
  (str author " (" year ")"))

(defn- models->events [new-or-edit]
  "Return a map from model (i.e., subscription) keyword (with :edited-settings
  ns) to event keyword (with :dativerf.events ns). The new-or-edit param is :new
  if this is for a new form or :edit for dealing with existing forms."
  (->> model/editable-keys
       (map (fn [k]
              [(keyword (str (name new-or-edit) "-form") k)
               (keyword "dativerf.events"
                        (str "user-changed-" (name new-or-edit) "-form-"
                             (name k)))]))
       (into {})))

(def ^:private new-models->events (models->events :new))
(def ^:private edit-models->events (models->events :edit))

(defn- model-event [form field]
  (let [model (keyword (str (if form "edit" "new") "-form") field)]
    [model ((if form edit-models->events new-models->events) model)]))

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
    (re-frame/dispatch [::events/user-clicked-create-new-form-button])))

(defn- key-up-input-edit [{:keys [uuid]} e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch [::events/user-clicked-update-form-button uuid])))

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

(defn- invalid-warning [{:keys [uuid]}]
  (when (= (if uuid ::edit-form-fsm/invalid ::new-form-fsm/invalid)
           @(re-frame/subscribe (if uuid
                                  [::subs/form-edit-state uuid]
                                  [:new-form/state])))
    [re-com/alert-box
     :alert-type :danger
     :heading (if uuid
                "Invalid Form (Update Failed)"
                "Invalid Form")
     :body @(re-frame/subscribe (if uuid
                                  [::subs/form-edit-general-validation-error-message uuid]
                                  [:new-form/general-validation-error-message]))]))

;; Buttons

(defn- save-button [{:as form :keys [uuid]}]
  (let [form-state @(re-frame/subscribe (if uuid
                                          [::subs/form-edit-state uuid]
                                          [:new-form/state]))
        changes? @(re-frame/subscribe
                   (if uuid
                     [::subs/edit-form-form-changed? uuid]
                     [::subs/new-form-form-changed?]))
        ready-state (if uuid ::edit-form-fsm/ready ::new-form-fsm/ready)
        disabled?
        (or (not= ready-state form-state)
            (not changes?))]
    [re-com/box
     :child
     [re-com/button
      :label "Save"
      :tooltip (if disabled?
                 "make some changes first"
                 (if form "save changes to this form" "create this new form"))
      :disabled? disabled?
      :on-click (fn [_] (re-frame/dispatch
                         (if uuid
                           [::events/user-clicked-update-form-button uuid]
                           [::events/user-clicked-create-new-form-button])))]]))

(defn- toggle-secondary-inputs-button [{:keys [uuid]}]
  [re-com/md-circle-icon-button
   :md-icon-name
   (if @(re-frame/subscribe
         (if uuid
           [::subs/form-edit-secondary-fields-visible? uuid]
           [::subs/forms-new-form-secondary-fields-visible?]))
     "zmdi-chevron-up"
     "zmdi-chevron-down")
   :size :smaller
   :tooltip
   (if @(re-frame/subscribe
         (if uuid
           [::subs/form-edit-secondary-fields-visible? uuid]
           [::subs/forms-new-form-secondary-fields-visible?]))
     "hide the secondary data input fields"
     "show the secondary data input fields")
   :on-click
   (fn [_] (re-frame/dispatch
            (if uuid
              [::events/user-clicked-toggle-secondary-edit-form-fields uuid]
              [::events/user-clicked-toggle-secondary-new-form-fields])))])

(defn- hide-interface-button [{:as form :keys [uuid]}]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-close"
   :size :smaller
   :tooltip (if form "hide edit form interface" "hide new form interface")
   :on-click
   (fn [_]
     (re-frame/dispatch
      (if uuid
        [::events/user-clicked-edit-form-button uuid]
        [::events/user-clicked-new-form-button])))])

(defn- reset-values-button [{:keys [uuid]}]
  (let [changes? @(re-frame/subscribe
                   (if uuid
                     [::subs/edit-form-form-changed? uuid]
                     [::subs/new-form-form-changed?]))]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-undo"
     :size :smaller
     :tooltip (if changes?
                "clear this form: reset all fields to their default values"
                "nothing to undo: make some changes first")
     :disabled? (not changes?)
     :on-click
     (fn [_]
       (re-frame/dispatch
        (if uuid
          [::events/user-clicked-clear-edit-form-interface uuid]
          [::events/user-clicked-clear-new-form-interface])))]))

(defn- help-mutating-form-button [form]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-help"
   :size :smaller
   :tooltip (if form
              "help with editing a form"
              "help with creating a new form")
   :disabled? true
   :on-click
   (fn [_]
     (re-frame/dispatch
      (if form
        [::events/user-clicked-help-editing-existing-form]
        [::events/user-clicked-help-creating-new-form])))])

(defn- reset-date-elicited-button [form]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-delete"
   :size :smaller
   :tooltip "remove date elicited"
   :on-click
   (fn [_]
     (re-frame/dispatch
      (if form
        [::events/user-changed-new-form-date-elicited nil]
        [::events/user-changed-edit-form-date-elicited (:uuid form) nil])))])

(defn- add-new-translation-button [{:keys [uuid]}]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-plus"
   :size :smaller
   :tooltip "add another translation"
   :attr {:tab-index "0"
          :on-key-down
          (fire-on-enter-space
           (if uuid
             [::events/user-clicked-add-new-translation-button-to-edit-form uuid]
             [::events/user-clicked-add-new-translation-button]))}
   :on-click
   (fn [_]
     (re-frame/dispatch
      (if uuid
        [::events/user-clicked-add-new-translation-button-to-edit-form uuid]
        [::events/user-clicked-add-new-translation-button])))])

(defn- remove-translation-button [index {:keys [uuid]}]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-minus"
   :size :smaller
   :tooltip "remove this translation"
   :attr {:tab-index "0"
          :on-key-down
          (fire-on-enter-space
           (if uuid
             [::events/user-clicked-remove-translation-button-from-edit-form index uuid]
             [::events/user-clicked-remove-translation-button index]))}
   :on-click
   (fn [_]
     (re-frame/dispatch
      (if uuid
        [::events/user-clicked-remove-translation-button-from-edit-form index uuid]
        [::events/user-clicked-remove-translation-button index])))])

;; Inputs

(defn- text-input
  ([field] (text-input field nil))
  ([field {:as form :keys [uuid]}]
   (let [[model event] (model-event form field)
         subscription (if form [model uuid] [model])]
     (when (some #{(utils/set-kw-ns-to-form model)}
                 @(re-frame/subscribe [::subs/visible-form-fields]))
       (let [invalid-msg
             @(re-frame/subscribe
               (if uuid
                 [::subs/form-edit-field-specific-validation-error-messages field uuid]
                 [:new-form/field-specific-validation-error-message field]))]
         [labeled-el
          field
          [re-com/input-text
           :change-on-blur? false
           :placeholder (placeholder field)
           :width "560px"
           :model @(re-frame/subscribe subscription)
           :status (and invalid-msg :error)
           :status-icon? invalid-msg
           :status-tooltip invalid-msg
           :attr {:on-key-up (if form
                               (partial key-up-input-edit form)
                               key-up-input)}
           :on-change
           (fn [value]
             (re-frame/dispatch-sync
              (if form [event uuid value] [event value])))]])))))

(defn- named-resource-single-select
  ([field form] (named-resource-single-select field form {}))
  ([field {:as form :keys [uuid]}
    {:keys [filter-box? allow-empty?] :or {filter-box? false
                                           allow-empty? true}}]
   (let [[model event] (model-event form field)]
     (when (some #{(utils/set-kw-ns-to-form model)}
                 @(re-frame/subscribe [::subs/visible-form-fields]))
       (let [label-fn (field->label-fn field :name)
             choices (let [tmp (field->choices field)]
                       (if (keyword? tmp)
                         @(re-frame/subscribe [tmp])
                         tmp))
             invalid-msg
             @(re-frame/subscribe
               (if form
                 [::subs/form-edit-field-specific-validation-error-messages field uuid]
                 [:new-form/field-specific-validation-error-message field]))]
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
             :model @(re-frame/subscribe (if uuid [model uuid] [model]))
             ;; This style is hacky, but it's the best I could do
             :style (when invalid-msg {:border "1px solid #d50000"
                                       :border-radius "5px"})
             :on-change (fn [value] (re-frame/dispatch
                                     (if uuid [event uuid value] [event value])))]
            (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]])))))

(defn- grammaticality [{:keys [id uuid]}]
  [re-com/single-dropdown
   :choices (for [g (distinct @(re-frame/subscribe [::subs/grammaticalities]))]
              ^{:key (let [key (str "transcription-grammaticality-" g)]
                       (if id (str "form-" id "-" key) key))}
              {:id g :label g})
   :width "50px"
   :model @(re-frame/subscribe (if uuid
                                 [:edit-form/grammaticality uuid]
                                 [:new-form/grammaticality]))
   :on-change (fn [grammaticality]
                (re-frame/dispatch
                 (if uuid
                   [::events/user-changed-edit-form-grammaticality uuid grammaticality]
                   [::events/user-changed-new-form-grammaticality grammaticality])))])

(defn- transcription [{:as form :keys [uuid]}]
  (let [invalid-msg
        @(re-frame/subscribe
          (if uuid
            [::subs/form-edit-field-specific-validation-error-messages :transcription uuid]
            [:new-form/field-specific-validation-error-message :transcription]))]
    [re-com/input-text
     :change-on-blur? false
     :placeholder "transcription"
     :width "500px"
     :model @(re-frame/subscribe (if uuid
                                   [:edit-form/transcription uuid]
                                   [:new-form/transcription]))
     :status (and invalid-msg :error)
     :status-icon? invalid-msg
     :status-tooltip invalid-msg
     :attr {:auto-focus true
            :on-key-up (if form
                         (partial key-up-input-edit form)
                         key-up-input)}
     :on-change
     (fn [transcription]
       (re-frame/dispatch-sync
        (if uuid
          [::events/user-changed-edit-form-transcription uuid transcription]
          [::events/user-changed-new-form-transcription transcription])))]))

(defn- grammaticality-transcription [form]
  [labeled-el
   :transcription
   [re-com/h-box
    :gap "10px"
    :children [[grammaticality form]
               [transcription form]]]])

(defn- translation-grammaticality [index {:keys [id uuid]}]
  [re-com/single-dropdown
   :choices (for [g (distinct @(re-frame/subscribe [::subs/grammaticalities]))]
              ^{:key (let [key (str "translation-" index "-grammaticality-" g)]
                       (if id (str "form-" id "-" key) key))}
              {:id g :label g})
   :width "50px"
   :model @(re-frame/subscribe
            (if uuid
              [:edit-form/translation-grammaticality index uuid]
              [:new-form/translation-grammaticality index]))
   :on-change (fn [grammaticality]
                (re-frame/dispatch
                 (if uuid
                   [::events/user-changed-edit-form-translation-grammaticality
                    index uuid grammaticality]
                   [::events/user-changed-new-form-translation-grammaticality
                    index grammaticality])))])

(defn- translation-transcription [index {:as form :keys [uuid]}]
  (let [invalid-msg
        @(re-frame/subscribe
          (if uuid
            [::subs/form-edit-field-specific-validation-error-messages :translations uuid]
            [:new-form/field-specific-validation-error-message :translations]))]
    [re-com/input-text
     :change-on-blur? false
     :placeholder "translation"
     :width "460px"
     :model @(re-frame/subscribe
              (if uuid
                [:edit-form/translation-transcription index uuid]
                [:new-form/translation-transcription index]))
     :status (and invalid-msg :error)
     :status-icon? invalid-msg
     :status-tooltip invalid-msg
     :attr {:on-key-up (if form
                         (partial key-up-input-edit form)
                         key-up-input)}
     :on-change
     (fn [transcription]
       (re-frame/dispatch-sync
        (if uuid
          [::events/user-changed-edit-form-translation-transcription
           index uuid transcription]
          [::events/user-changed-new-form-translation-transcription
           index transcription])))]))

(defn- add-remove-translation-button [index form]
  [re-com/box
   :class (styles/default)
   :child (if (zero? index)
            [add-new-translation-button form]
            [remove-translation-button index form])])

(defn- grammaticality-translation [index form]
  [labeled-el
   (when (zero? index) :translations)
   [re-com/h-box
    :gap "10px"
    :children
    [[translation-grammaticality index form]
     [translation-transcription index form]
     [add-remove-translation-button index form]]]])

(defn- translations [{:as form :keys [uuid id]}]
  [re-com/v-box
   :gap "10px"
   :children
   (for [index (range (count @(re-frame/subscribe
                               (if form
                                 [:edit-form/translations uuid]
                                 [:new-form/translations]))))]
     ^{:key (let [key (str "translation-" index)]
              (if id (str "form-" id "-" key) key))}
     [grammaticality-translation index form])])

(defn- tags [{:keys [uuid]}]
  (when (some #{:form/tags} @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [available-tags @(re-frame/subscribe [::subs/mini-tags])
          invalid-msg
          @(re-frame/subscribe
            (if uuid
              [::subs/form-edit-field-specific-validation-error-messages :tags uuid]
              [:new-form/field-specific-validation-error-message :tags]))]
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
          :model @(re-frame/subscribe
                   (if uuid [:edit-form/tags uuid] [:new-form/tags]))
          :on-change (fn [tags]
                       (re-frame/dispatch
                        (if uuid
                          [::events/user-changed-edit-form-tags uuid tags]
                          [::events/user-changed-new-form-tags tags])))]
         (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]])))

(defn- date-elicited [{:as form :keys [uuid]}]
  (when (some #{:form/date-elicited}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    (let [invalid-msg
          @(re-frame/subscribe
            (if uuid
              [::subs/form-edit-field-specific-validation-error-messages :date-elicited uuid]
              [:new-form/field-specific-validation-error-message :date-elicited]))]
      [re-com/h-box
       :class (styles/default)
       :gap "10px"
       :children
       [[label :date-elicited]
        ;; TODO: the datepicker of re-com doesn't work that well. For instance, the
        ;; :show-today? attribute doesn't work. This seems to be a known issue.
        [re-com/datepicker-dropdown
         :model @(re-frame/subscribe (if uuid
                                       [:edit-form/date-elicited uuid]
                                       [:new-form/date-elicited]))
         :show-today? true
         :on-change (fn [date-elicited]
                      (re-frame/dispatch
                       (if uuid
                         [::events/user-changed-edit-form-date-elicited uuid date-elicited]
                         [::events/user-changed-new-form-date-elicited date-elicited])))]
        [reset-date-elicited-button form]
        (when invalid-msg [widgets/field-invalid-warning invalid-msg])]])))

(defn- inputs [form]
  [re-com/v-box
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[text-input :narrow-phonetic-transcription form]
    [text-input :phonetic-transcription form]
    [grammaticality-transcription form]
    [text-input :morpheme-break form]
    [text-input :morpheme-gloss form]
    [translations form]]])

(defn- secondary-inputs [form]
  (when @(re-frame/subscribe
          (if form
            [::subs/form-edit-secondary-fields-visible? (:uuid form)]
            [::subs/forms-new-form-secondary-fields-visible?]))
    [re-com/v-box
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[text-input :comments form]
      [text-input :speaker-comments form]
      [named-resource-single-select :elicitation-method form]
      [tags form]
      [named-resource-single-select :syntactic-category form]
      [date-elicited form]
      [named-resource-single-select :speaker form]
      [named-resource-single-select :elicitor form]
      [named-resource-single-select :verifier form]
      [named-resource-single-select :source form {:filter-box? true}]
      ;; TODO: files select. The original Dative has a custom search interface
      ;; that hits the POST /files/search endpoint to return the selectable
      ;; options.
      [text-input :syntax form]
      [text-input :semantics form]
      [named-resource-single-select :status form {:allow-empty? false}]]]))

;; Headers and Footers

(defn- header-title [form]
  (if form
    [re-com/box :child (str "Edit Form " (:id form))]
    [re-com/box :child "New Form"]))

(defn- header [form]
  (widgets/header
   {:left [[hide-interface-button form]
           [toggle-secondary-inputs-button form]]
    :center [[header-title form]]
    :right [[reset-values-button form]
            [help-mutating-form-button form]]}))

(defn- footer [form]
  (widgets/footer
   {:center [[save-button form]
             [toggle-secondary-inputs-button form]]}))

(defn interface
  "A nil form indicates the interface is for creating a new form. A non-nil form
  indicates the interface is for editing an existing form."
  ([] (interface nil))
  ([form]
   (when @(re-frame/subscribe
           (if form
             [::subs/form-edit-interface-visible? (:uuid form)]
             [::subs/forms-new-form-interface-visible?]))
     (re-frame/dispatch [::events/fetch-new-form-data]) ;; caching makes this often a no-op
     [re-com/v-box
      :gap "10px"
      :class (styles/form-sub-interface)
      :children
      [[header form]
       [invalid-warning form]
       [inputs form]
       [secondary-inputs form]
       [footer form]]])))
