(ns dativerf.views.old-settings
  (:require [reagent.ratom :as r]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.models.application-settings :as model]
            [dativerf.models.utils :as mutils]
            [dativerf.routes :as routes]
            [dativerf.specs.application-settings :as spec]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]
            [clojure.string :as str]
            [clojure.set :as set]))

;; Utils

(def standard-input-width "460px")
(def errored-input-width "425px")

(def ^:private invalid-style
  {:border "1px solid #d50000"
   :border-radius "5px"})

(def ^:private language-correspondences
  "Connection between a language name and ID."
  {:edited-settings/object-language-name :edited-settings/object-language-id
   :edited-settings/metalanguage-name :edited-settings/metalanguage-id})

(def ^:private models->events
  "Map from model (i.e., subscription) keyword (with :edited-settings ns) to
  event keyword (with :dativerf.events ns)."
  (->> model/editable-keys
       (map (fn [k]
              [(keyword "edited-settings" k)
               (keyword "dativerf.events"
                        (str "user-changed-settings-" (name k)))]))
       (into {})))

(def ^:private settings-types
  "Map from types of (subsets of) application settings and vectors containing a
  description of the subset, the event for toggling the display, and the
  subscription to determine whether the edit display is visible."
  {:general
   ["general settings"
    ::events/user-clicked-edit-general-settings-button
    ::subs/general-settings-edit-interface-visible?]
   :input-validation
   ["input validation settings"
    ::events/user-clicked-edit-input-validation-settings-button
    ::subs/input-validation-settings-edit-interface-visible?]})

(defn- field-event [model]
  [(utils/remove-namespace model)
   (models->events model)])

;; View Affordances

(def ^:private labeled-el (partial widgets/labeled-el model/settings-metadata))

(def ^:private label-with-tooltip
  (partial widgets/label-with-tooltip model/settings-metadata))

;; Buttons

(defn- undo-settings-changes-button [settings-type]
  (let [[_ _ subscription] (settings-types settings-type)]
    (when @(re-frame/subscribe [subscription])
      (let [changes-made? @(re-frame/subscribe [:settings/edited-settings-changed?])]
        [re-com/md-circle-icon-button
         :md-icon-name "zmdi-undo"
         :size :smaller
         :tooltip (if changes-made?
                    "restore to the current settings"
                    "no changes yet: nothing to restore")
         :disabled? (not changes-made?)
         :on-click (fn [_]
                     (re-frame/dispatch
                      [::events/user-clicked-clear-edit-settings-interface]))]))))

(defn toggle-view-edit-button
  "Create a button for switching between the display view and edit view of a
  subset of the application settings. The settings-type param is a keyword in
  the keys of settings-types."
  [settings-type]
  (let [[settings-label event subscription] (settings-types settings-type)
        edit-visible? @(re-frame/subscribe [subscription])]
    [re-com/md-circle-icon-button
     :md-icon-name (if edit-visible? "zmdi-chevron-left" "zmdi-edit")
     :size :smaller
     :tooltip (str (if edit-visible? "view the " "edit these ") settings-label)
     :on-click (fn [_] (re-frame/dispatch [event]))]))

(defn settings-type-title
  [settings-type]
  (let [[settings-label _ subscription] (settings-types settings-type)]
    [re-com/box
     :child (utils/capitalize-words
             (str
              (if @(re-frame/subscribe [subscription]) "edit " "view ")
              settings-label))]))

(defn save-button []
  (let [changes-made? @(re-frame/subscribe [:settings/edited-settings-changed?])]
    [re-com/box
     :child
     [re-com/button
      :label "Save"
      :tooltip (if changes-made?
                 "save changes to the settings"
                 "make some changes first")
      :disabled? (or (not changes-made?)
                     (not= :dativerf.fsms.edit-settings/ready
                           @(re-frame/subscribe [:edited-settings/state])))
      :on-click (fn [_e] (re-frame/dispatch
                          [::events/user-clicked-save-application-settings-button]))]]))

;; View Widgets

(defn- title []
  [re-com/title
   :src   (at)
   :label (str @(re-frame/subscribe [::subs/old-name]) " Settings")
   :level :level2])

(defn- stringify-grapheme-info [graphemes]
  (->> graphemes
       (map (fn [{:keys [unicode name]}]
              (str unicode (when name (str " " name)))))
       (str/join ", ")))

(defn- graph-display
  "For displaying a 'graph' qua a series of graphemes that represent a single
  'letter' in an orthography."
  [{:keys [graph graphemes]} {:keys [last? delimiter]}]
  (let [showing? (r/atom false)
        tooltip (stringify-grapheme-info graphemes)]
    [re-com/popover-tooltip
     :label tooltip
     :showing? showing?
     :width (when (> (count tooltip) 80) "400px")
     :anchor
     [re-com/box
      :class (styles/objlang)
      :style (when-not last? {:padding-right "6px"})
      :attr {:on-mouse-over (re-com/handler-fn (reset! showing? true))
             :on-mouse-out (re-com/handler-fn (reset! showing? false))}
      :child (if last? graph (str graph delimiter))]]))

(defn- inventory-display
  "Display an inventory. The inventory is a string representing a sequence of
  graphs. We display it as a <div> for each graph, each having a tooltip that
  shows the Unicode code points and names of each grapheme in the graph. The
  parser for parsing the inventory and the delimiter for separating the graph
  displays can be configured in the map second parameter."
  ([inventory] (inventory-display inventory {}))
  ([inventory {:keys [delimiter parser] :or {parser model/parse-inventory
                                             delimiter ","}}]
   (let [parsed (parser inventory @(re-frame/subscribe [::subs/unicode-data]))
         last-idx (dec (count parsed))]
     [:div
      {:style {:max-width "400px"}}
      (for [[idx {:as graph-data :keys [graph]}]
            (map vector (range) parsed)]
        ^{:key (str graph idx)}
        [graph-display graph-data {:last? (= last-idx idx)
                                   :delimiter delimiter}])])))

;; Input Widgets

(defn- validation-select [model]
  (let [[field event] (field-event model)
        invalid-msg @(re-frame/subscribe
                      [:settings-edit/field-specific-validation-error-message
                       field])]
    [labeled-el field
     [re-com/h-box
      :children
      [[re-com/single-dropdown
        :choices (sort-by :label (for [v spec/validation-values]
                                   {:id v :label v}))
        :width (if invalid-msg errored-input-width standard-input-width)
        :model (or @(re-frame/subscribe [model]) "None")
        :style (when invalid-msg invalid-style)
        :on-change (fn [value] (re-frame/dispatch [event value]))]
       (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]]))

(defn- checkbox [model]
  (let [[field event] (field-event model)]
    [labeled-el
     field
     [re-com/h-box
      :children
      [[re-com/checkbox
        :model @(re-frame/subscribe [model])
        :on-change (fn [value] (re-frame/dispatch [event value]))]]]]))

(defn- orthography-select [model]
  (let [[field event] (field-event model)
        invalid-msg @(re-frame/subscribe
                      [:settings-edit/field-specific-validation-error-message
                       field])
        orthographies @(re-frame/subscribe [::subs/mini-orthographies])]
    [labeled-el
     field
     [re-com/h-box
      :children
      [[re-com/single-dropdown
        :choices (concat [{:id nil :label "None"}]
                         (sort-by :label (for [o orthographies]
                                           {:id (:id o) :label (:name o)})))
        :width (if invalid-msg errored-input-width standard-input-width)
        :model (or @(re-frame/subscribe [model]) "")
        :style (when invalid-msg invalid-style)
        :on-change (fn [value] (re-frame/dispatch [event value]))]
       (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]]))

(defn- text-input
  [model]
  (let [[field event] (field-event model)]
    [widgets/text-input
     model/settings-metadata
     model
     field
     event
     :settings-edit/field-specific-validation-error-message
     {:submit-event ::events/user-clicked-save-application-settings-button}]))

;; TODO: clever out the seemingly redundant second iteration through languages
(defn- match-language
  "Return the first 8 languages that match search term s. Place any exact match
  at the start. Sort the rest alphabetically."
  [keys-vals s]
  (let [term (str/lower-case s)
        languages @(re-frame/subscribe [::subs/languages])
        substring-matches
        (->> languages keys-vals (filter (fn [x] (str/includes? (str/lower-case
                                                                 x) term))))
        exact-matches
        (->> languages keys-vals (filter (fn [x] (= term (str/lower-case x)))))]
    (->> (concat exact-matches substring-matches)
         (sort-by (fn [v] [(not= s v) v]))
         (take 8)
         set
         (sort-by (fn [v] [(not= s v) v])))))

(def match-language-name (partial match-language vals))
(def match-language-id (partial match-language keys))

(defn- suggested-counterpart
  "Display a suggested counterpart for a user-selected language-related value.
  For example, if the user has specified a specific ISO 639-3 3-character
  language code for the object language, then this widget would display the
  corresponding language Ref_Name and a checkmark button. If the user clicks
  that button, then the object language name would receive the Ref_Name
  counterpart value."
  [model model-type metadata]
  (let [languages @(re-frame/subscribe [::subs/languages])
        value @(re-frame/subscribe [model])
        counterpart (get language-correspondences model
                         (get (set/map-invert language-correspondences) model))
        counterpart-event (models->events counterpart)
        counterpart-value @(re-frame/subscribe [counterpart])
        counterpart-label (mutils/label-str metadata (utils/remove-namespace counterpart))
        tooltip (str "use this " counterpart-label " value")
        finder (if (= model-type :name) (set/map-invert languages) languages)
        suggestion (get finder value)]
    (when (and suggestion (not= suggestion counterpart-value))
      [re-com/h-box
       :gap "10px"
       :children
       [[re-com/box :child suggestion]
        [re-com/md-circle-icon-button
         :md-icon-name "zmdi-check"
         :size :smaller
         :class (styles/default)
         :tooltip tooltip
         :on-click (fn [_] (re-frame/dispatch [counterpart-event suggestion]))]]])))

(defn language-typeahead
  "Display for typing a language Id or Name and having auto-completion from the
  ISO 639-3 languages dataset."
  [model-type metadata model]
  (let [[field event] (field-event model)
        data-source-fn (if (= :name model-type)
                         match-language-name
                         match-language-id)
        invalid-msg @(re-frame/subscribe
                      [:settings-edit/field-specific-validation-error-message
                       field])]
    [labeled-el
     field
     [re-com/h-box
      :gap "10px"
      :children
      [[re-com/typeahead
        :width "260px"
        :placeholder (mutils/placeholder metadata field)
        :debounce-delay 600
        :data-source data-source-fn
        :on-change (fn [val] (re-frame/dispatch-sync [event val]))
        :change-on-blur? true
        :status (and invalid-msg :error)
        :status-icon? invalid-msg
        :status-tooltip invalid-msg
        :model @(re-frame/subscribe [model])]
       [suggested-counterpart model model-type metadata]]]]))

(def language-name-typeahead
  (partial language-typeahead :name model/settings-metadata))

(def language-id-typeahead
  (partial language-typeahead :id model/settings-metadata))

(defn unrestricted-users-select
  "Display for selection a set of unrestricted users."
  []
  (let [field :unrestricted-users
        invalid-msg @(re-frame/subscribe
                      [:settings-edit/field-specific-validation-error-message
                       field])
        mini-users @(re-frame/subscribe [::subs/mini-users])]
    [labeled-el field
     (if mini-users
       [re-com/h-box
        :children
        [[re-com/selection-list
          :choices (sort-by :label
                            (for [{:keys [id first-name last-name]} mini-users]
                              {:id id :label (str first-name " " last-name)}))
          :width (if invalid-msg "423px" "458px")
          :height "60px"
          :model @(re-frame/subscribe [:edited-settings/unrestricted-users])
          :on-change (fn [users]
                       (re-frame/dispatch
                        [::events/user-changed-settings-unrestricted-users users]))]
         (when invalid-msg [widgets/field-invalid-warning invalid-msg])]]
       [re-com/throbber])]))

;; Composite Views

(defn- general-settings-view []
  (let [{:keys [object-language-name object-language-id metalanguage-name
                metalanguage-id unrestricted-users datetime-modified]}
        @(re-frame/subscribe [::subs/old-settings])]
    [re-com/v-box
     :src (at)
     :gap "1em"
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[widgets/v-box [[labeled-el :object-language-name
                       [re-com/box :child object-language-name]]
                      [labeled-el :object-language-id
                       [re-com/box :child object-language-id]]]]
      [widgets/v-box [[labeled-el :metalanguage-name
                       [re-com/box :child metalanguage-name]]
                      [labeled-el :metalanguage-id
                       [re-com/box :child metalanguage-id]]]]
      ;; TODO: users should be links to the user resources, but they don't exist
      ;; yet.
      [widgets/v-box [[labeled-el :unrestricted-users
                       (widgets/value-cell {:type :coll-of-users
                                            :value unrestricted-users})]
                      [labeled-el :datetime-modified
                       [re-com/box :child
                        (utils/datetime->human-string datetime-modified)]]]]]]))

(defn- general-settings-edit []
  (re-frame/dispatch [::events/fetch-settings-new-data]) ;; caching makes this often a no-op
  [re-com/v-box
   :src (at)
   :gap "1em"
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[widgets/v-box-gap-with-nils
     [[language-name-typeahead :edited-settings/object-language-name]
      [language-id-typeahead :edited-settings/object-language-id]]]
    [widgets/v-box-gap-with-nils
     [[language-name-typeahead :edited-settings/metalanguage-name]
      [language-id-typeahead :edited-settings/metalanguage-id]]]
    [widgets/v-box-gap-with-nils [[unrestricted-users-select]]]]])

(defn input-validation-settings-edit []
  (re-frame/dispatch [::events/fetch-settings-new-data])
  [re-com/v-box
   :src (at)
   :gap "1em"
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[widgets/v-box-gap-with-nils
     [[validation-select :edited-settings/orthographic-validation]
      [orthography-select :edited-settings/storage-orthography]]]
    [widgets/v-box-gap-with-nils
     [[validation-select :edited-settings/narrow-phonetic-validation]
      [text-input :edited-settings/narrow-phonetic-inventory]]]
    [widgets/v-box-gap-with-nils
     [[validation-select :edited-settings/broad-phonetic-validation]
      [text-input :edited-settings/broad-phonetic-inventory]]]
    [widgets/v-box-gap-with-nils
     [[validation-select :edited-settings/morpheme-break-validation]
      [checkbox :edited-settings/morpheme-break-is-orthographic]
      [text-input :edited-settings/phonemic-inventory]
      [text-input :edited-settings/morpheme-delimiters]]]
    [widgets/v-box-gap-with-nils
     [[text-input :edited-settings/punctuation]
      [text-input :edited-settings/grammaticalities]]]]])

(defn- input-validation-settings-view []
  (let [{:keys [broad-phonetic-inventory broad-phonetic-validation
                grammaticalities morpheme-break-is-orthographic
                morpheme-break-validation morpheme-delimiters
                narrow-phonetic-inventory narrow-phonetic-validation
                orthographic-validation phonemic-inventory punctuation
                storage-orthography]}
        @(re-frame/subscribe [::subs/old-settings])]
    [re-com/v-box
     :src (at)
     :gap "1em"
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[widgets/v-box [[labeled-el :orthographic-validation
               [re-com/box :child (or orthographic-validation "None")]]
              ;; TODO: storage orthography should be a link to the orthography
              ;; resource, but vws for orthographies don't exist yet.
              [labeled-el :storage-orthography
               [inventory-display (or (:orthography storage-orthography) "")]]]]
      [widgets/v-box [[labeled-el :narrow-phonetic-validation
               [re-com/box :child (or narrow-phonetic-validation "None")]]
              [labeled-el :narrow-phonetic-inventory
               [inventory-display narrow-phonetic-inventory]]]]
      [widgets/v-box [[labeled-el :broad-phonetic-validation
               [re-com/box :child (or broad-phonetic-validation "None")]]
              [labeled-el :broad-phonetic-inventory
               [inventory-display broad-phonetic-inventory]]]]
      [widgets/v-box [[labeled-el :morpheme-break-validation
               [re-com/box :child (or morpheme-break-validation "None")]]
              [labeled-el :morpheme-break-is-orthographic
               [re-com/box :child (str morpheme-break-is-orthographic)]]
              [labeled-el :phonemic-inventory
               [inventory-display phonemic-inventory]]
              [labeled-el :morpheme-delimiters
               [inventory-display morpheme-delimiters]]]]
      [widgets/v-box [[labeled-el :punctuation
               [inventory-display punctuation {:parser model/parse-punctuation
                                               :delimiter ""}]]
              [labeled-el :grammaticalities
               [inventory-display grammaticalities]]]]]]))

;; Headers & Footers

(defn footer []
  (widgets/footer
   {:center [[save-button]]}))

(defn general-header []
  (widgets/header
   {:left [[toggle-view-edit-button :general]]
    :center [[settings-type-title :general]]
    :right [[undo-settings-changes-button :general]]}))

(defn input-validation-header []
  (widgets/header
   {:left [[toggle-view-edit-button :input-validation]]
    :center [[settings-type-title :input-validation]]
    :right [[undo-settings-changes-button :input-validation]]}))

(defn- server-settings-subtab []
  (let [edit? @(re-frame/subscribe [::subs/general-settings-edit-interface-visible?])]
    [re-com/v-box
     :gap "10px"
     :children
     [[general-header]
      (if edit? [general-settings-edit] [general-settings-view])
      (when edit? [footer])]]))

(defn- input-validation-subtab []
  (let [edit? @(re-frame/subscribe [::subs/input-validation-settings-edit-interface-visible?])]
    [re-com/v-box
     :gap "10px"
     :children
     [[input-validation-header]
      (if edit? [input-validation-settings-edit] [input-validation-settings-view])
      (when edit? [footer])]]))

(defmulti settings-subtabs :handler)

(defmethod settings-subtabs :old-settings []
  [server-settings-subtab])

(defmethod settings-subtabs :old-settings-input-validation []
  [input-validation-subtab])

(def ^:private submenu-tabs
  [{:id :old-settings
    :label "General Settings"}
   {:id :old-settings-input-validation
    :label "Input Validation"}])

(defn- menu [{:keys [handler]}]
  [re-com/horizontal-tabs
   :src (at)
   :tabs submenu-tabs
   :model handler
   :on-change (fn [tab-id]
                (re-frame/dispatch
                 [::events/navigate
                  {:handler tab-id
                   :route-params {:old @(re-frame/subscribe [::subs/old-slug])}}]))])

(defn- settings-tab [route]
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[title]
    [menu route]
    (settings-subtabs route)]])

(defn- old-settings [route]
  (if (and @(re-frame/subscribe [::subs/old-settings])
           @(re-frame/subscribe [::subs/unicode-data]))
    [settings-tab route]
    (do (re-frame/dispatch [::events/fetch-applicationsettings])
        (re-frame/dispatch [::events/fetch-unicode-data])
        [re-com/throbber :size :large])))

(defmethod routes/tabs :old-settings [route]
  (old-settings route))

(defmethod routes/tabs :old-settings-input-validation [route]
  (old-settings route))
