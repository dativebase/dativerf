(ns dativerf.views.old-settings
  (:require [reagent.ratom :as r]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.models.old :as old-model]
            [dativerf.models.application-settings :as model]
            [dativerf.routes :as routes]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]
            [clojure.string :as str]))

(defn- title []
  (let [old-id @(re-frame/subscribe [::subs/old])
        olds @(re-frame/subscribe [::subs/olds])
        old-name (old-model/name* {:old old-id :olds olds})]
    [re-com/title
     :src   (at)
     :label (str old-name " Settings")
     :level :level2]))

;; Tooltips and Labels for Settings Fields

(def validation-meaning
  (str "'None' means no validation."
       " 'Warning' means a warning is generated when a user tries invalid"
       " input."
       " 'Error' means invalid input is forbidden."))

(defn- inventory-tooltip
  ([preamble field] (inventory-tooltip preamble field ""))
  ([preamble field postamble]
   (str preamble
        "A comma-delimited list of graphemes that should be used when entering"
        " data into the "
        field
        " field"
        postamble
        ".")))

(defn- language-name-tooltip [description]
  (str "The name of the language that "
       description ". This may be the ISO 639-3 \u201creference name\u201d but"
       " this is not required."))

(defn- language-id-tooltip [description]
  (str "The three-letter ISO 639-3 identifier for the language that "
       description ". This field may be left blank if no such identifer is"
       " appropriate."))

(def object-language-description
  "is being documented and analyzed by means of this OLD web service")

(def metalanguage-description
  "is being used to translate, document and analyze the object language")

(def settings-metadata
  {:broad-phonetic-inventory
   {:tooltip (inventory-tooltip "" "phonetic transcription")}
   :broad-phonetic-validation
   {:tooltip (str "How to validate user input in the 'phonetic"
                  " transcription' field. " validation-meaning)}
   :datetime-modified
   {:tooltip (str "When these settings were last modified.")}
   :grammaticalities
   {:tooltip (str "A comma-delimited list of characters that will define the"
                  " options in the grammaticality fields. Example:"
                  " \u201c*,?,#\u201d.")}
   :metalanguage-id
   {:tooltip (language-id-tooltip metalanguage-description)}
   :metalanguage-name
   {:tooltip (language-name-tooltip metalanguage-description)}
   :morpheme-break-is-orthographic
   {:label "morph. break orthographic?"
    :tooltip (str "Morpheme break is orthographic? If set to true, this means"
                  " that the morpheme break field should be validated against"
                  " the storage orthography. If set to false, it means that it"
                  " should be validated against the phonemic inventory.")}
   :morpheme-break-validation
   {:tooltip (str "How to validate user input in the 'morpheme break' field. "
                  validation-meaning)}
   :morpheme-delimiters
   {:tooltip (str "A comma-delimited list of delimiter"
                  " characters that should be used to separate morphemes in the"
                  " morpheme break field and morpheme glosses in the morpheme"
                  " gloss field.")}
   :narrow-phonetic-inventory
   {:tooltip (inventory-tooltip "" "narrow phonetic transcription")}
   :narrow-phonetic-validation
   {:tooltip (str "How to validate user input in the 'narrow phonetic"
                  " transcription' field. " validation-meaning)}
   :object-language-id
   {:tooltip (language-id-tooltip object-language-description)}
   :object-language-name
   {:tooltip (language-name-tooltip object-language-description)}
   :orthographic-validation
   {:tooltip (str "How to validate user input in the 'transcription' field. "
                  validation-meaning)}
   :phonemic-inventory
   {:tooltip (inventory-tooltip
              "" "morpheme break"
              " (assuming 'morpheme break is orthographic' is set to false)")}
   :punctuation
   {:tooltip (str "A string of punctuation characters that should define, along"
                  " with the graphemes in the storage orthography, the licit"
                  " strings in the transcription field.")}
   :storage-orthography
   {:tooltip (str "The orthography that transcription values should be stored"
                  " in. This orthography may affect how orthographic validation"
                  " works and/or how orthography conversion works.")}
   :unrestricted-users
   {:tooltip (str "A list of users that the OLD server considers to be"
                  " \u201cunrestricted\u201d. These users are able to access"
                  " data that has been tagged with the \u201crestricted\u201d"
                  " tag.")}})

;; General-purpose View Machinery

;; TODO: this is copied and modified from views.forms.new. This should be
;; generalized and put in widgets maybe?
(defn get-x-from-metadata [x metadata attr]
  (or (-> attr metadata x)
      (-> attr name utils/kebab->space)))
(def label-str (partial get-x-from-metadata :label))
(def placeholder (partial get-x-from-metadata :placeholder))
(def tooltip (partial get-x-from-metadata :tooltip))

(defn label [attr metadata]
  (let [showing? (r/atom false)
        tooltip* (tooltip metadata attr)]
    [re-com/popover-tooltip
     :label tooltip*
     :showing? showing?
     :width (when (> (count tooltip*) 80) "400px")
     :anchor
     [re-com/box
      :class (str (styles/wider-attr-label) " " (styles/objlang) " "
                  (styles/actionable))
      :align :end
      :padding "0 1em 0 0"
      :attr {:on-mouse-over (re-com/handler-fn (reset! showing? true))
             :on-mouse-out (re-com/handler-fn (reset! showing? false))}
      :child (label-str metadata attr)]]))

(defn labeled-value [attr value-el]
  [re-com/h-box
   :gap "10px"
   :children
   [[label attr settings-metadata]
    value-el]])

(defn- v-box [els] [re-com/v-box :src (at) :children els])

;; Input Validation Display View

(defn- stringify-grapheme-info [graphemes]
  (->> graphemes
       (map (fn [{:keys [unicode name]}]
              (str unicode (when name (str " " name)))))
       (str/join ", ")))

(defn graph-display
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

(defn inventory-display
  "The inventory is a sequences of graphs. We display it as a <div> for each
  graph, each having a tooltip that shows the Unicode code points and names of
  each grapheme in the graph."
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

(defn- input-validation-subtab []
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
     [[v-box [[labeled-value :orthographic-validation
               [re-com/box :child orthographic-validation]]
              ;; TODO: storage orthography should be a link to the orthography
              ;; resource, but vws for orthographies don't exist yet.
              [labeled-value :storage-orthography
               [inventory-display (or (:orthography storage-orthography) "")]]]]
      [v-box [[labeled-value :narrow-phonetic-validation
               [re-com/box :child narrow-phonetic-validation]]
              [labeled-value :narrow-phonetic-inventory
               [inventory-display narrow-phonetic-inventory]]]]
      [v-box [[labeled-value :broad-phonetic-validation
               [re-com/box :child broad-phonetic-validation]]
              [labeled-value :broad-phonetic-inventory
               [inventory-display broad-phonetic-inventory]]]]
      [v-box [[labeled-value :morpheme-break-validation
               [re-com/box :child morpheme-break-validation]]
              [labeled-value :morpheme-break-is-orthographic
               [re-com/box :child (str morpheme-break-is-orthographic)]]
              [labeled-value :phonemic-inventory
               [inventory-display phonemic-inventory]]
              [labeled-value :morpheme-delimiters
               [inventory-display morpheme-delimiters]]]]
      [v-box [[labeled-value :punctuation
               [inventory-display punctuation {:parser model/parse-punctuation
                                               :delimiter ""}]]
              [labeled-value :grammaticalities
               [inventory-display grammaticalities]]]]]]))

;; General Settings Display View

(defn- server-settings-subtab []
  (let [{:keys [object-language-name object-language-id metalanguage-name
                metalanguage-id unrestricted-users datetime-modified]}
        @(re-frame/subscribe [::subs/old-settings])]
    [re-com/v-box
     :src (at)
     :gap "1em"
     :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
     :children
     [[v-box [[labeled-value :object-language-name
               [re-com/box :child object-language-name]]
              [labeled-value :object-language-id
               [re-com/box :child object-language-id]]]]
      [v-box [[labeled-value :metalanguage-name
               [re-com/box :child metalanguage-name]]
              [labeled-value :metalanguage-id
               [re-com/box :child metalanguage-id]]]]
      ;; TODO: users should be links to the user resources, but they don't exist
      ;; yet.
      [v-box [[labeled-value :unrestricted-users
               (widgets/value-cell {:type :coll-of-users
                                    :value unrestricted-users})]
              [labeled-value :datetime-modified
               [re-com/box :child
                (utils/datetime->human-string datetime-modified)]]]]]]))

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
