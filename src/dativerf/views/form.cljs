(ns dativerf.views.form
  (:require [cljs-time.format :as timef]
            [clojure.string :as str]
            [dativerf.events :as events]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.exporters.form :as form-exporter]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]))

;; Buttons

(defn export-button [form-id]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-download"
   :size :smaller
   :tooltip (if @(re-frame/subscribe [::subs/form-export-interface-visible? form-id])
              "hide export interface"
              "show export interface")
   :on-click (fn [_]
               (re-frame/dispatch
                [::events/user-clicked-export-form-button form-id]))])

(defn collapse-button [form-id]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-chevron-up"
   :size :smaller
   :tooltip "collapse this form"
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-form form-id]))])

(defn form-export-select [form-id]
  [re-com/single-dropdown
   :src (at)
   :width "250px"
   :choices form-exporter/exports
   :model @(re-frame/subscribe [::subs/form-export-format form-id])
   :tooltip "choose an export format"
   :on-change
   (fn [export-id]
     (re-frame/dispatch
      [::events/user-selected-form-export form-id export-id]))])

(defn delete-form-button [form-id]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-delete"
   :size :smaller
   :tooltip "delete this form"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-delete-form-button form-id]))])

;; End Buttons

(def form-value-width "500px")

(def attrs
  {:break-gloss-category
   {:label "break gloss category"
    :tooltip
    (str "The morpheme break, morpheme gloss and (syntactic) category"
         " string values all interleaved into a single string. This value is"
         " auto-generated by the application.")}

   :comments
   {:label "comments"
    :tooltip ""}

   :datetime-entered
   {:label "datetime entered"
    :tooltip ""}

   :datetime-modified
   {:label "datetime modified"
    :tooltip ""}

   :date-elicited
   {:label "date elicited"
    :tooltip ""}

   :elicitation-method
   {:label "elicitation method"
    :tooltip ""}

   :elicitor
   {:label "elicitor"
    :tooltip ""}

   :enterer
   {:label "enterer"
    :tooltip ""}

   :files
   {:label "files"
    :tooltip ""}

   :id
   {:label "ID"
    :tooltip
    (str "The ID of the form. This is an integer generated by the relational"
         " database that is used by the OLD. This value can be used to uniquely"
         " identify the form.")}

   :modifier
   {:label "modifier"
    :tooltip ""}

   :morpheme-break
   {:label "morpheme break"
    :tooltip
    (str "A sequence of morpheme shapes and delimiters. The OLD assumes"
         " phonemic shapes (e.g., “in-perfect”), but phonetic (i.e.,"
         " allomorphic, e.g., “im-perfect”) ones are ok.")}

   :morpheme-gloss
   {:label "morpheme gloss"
    :tooltip
    (str "A sequence of morpheme glosses and delimiters, isomorphic to"
         " the morpheme break sequence, e.g., “NEG-parfait”.")}

   :narrow-phonetic-transcription
   {:label "narr. phon. transcr."
    :full-label "narrow phonetic transcription"
    :tooltip ""}

   :phonetic-transcription
   {:label "phon. transcription"
    :full-label "phonetic transcription"
    :tooltip ""}

   :semantics
   {:label "semantics"
    :tooltip ""}

   :source
   {:label "source"
    :tooltip ""}

   :speaker
   {:label "speaker"
    :tooltip ""}

   :speaker-comments
   {:label "speaker comments"
    :tooltip ""}

   :status
   {:label "status"
    :tooltip
    (str "The status of the form: “tested” for data that have been"
         " elicited/tested/verified with a consultant or “requires testing” for"
         " data that are posited and still need testing/elicitation.")}

   :syntactic-category
   {:label "syntactic category"
    :tooltip ""}

   :syntactic-category-string
   {:label "syncat string"
    :tooltip
    (str "A sequence of categories (and morpheme delimiters) that is"
         " auto-generated by the system based on the morphemes/glosses entered"
         " into the morpheme break and morpheme gloss fields and the"
         " categories of matching lexical items in the database.")}

   :syntax
   {:label "syntax"
    :tooltip ""}

   :tags
   {:label "tags"
    :tooltip ""}

   :transcription
   {:label "transcription"
    :tooltip "A transcription, probably orthographic."}

   :translations
   {:label "translations"}

   :uuid
   {:label "UUID"
    :tooltip
    (str "The UUID (universally unique identifier) of the form."
         " This is a unique value generated by the OLD. It is used to create"
         " references between forms and their previous versions.")}
   :verifier
   {:label "verifier"
    :tooltip ""}})

(defn igt-label [label]
  [re-com/box
   :class (styles/attr-label)
   :justify :end
   :padding "0 1em 0 0"
   :child label])

(defn igt-transcription
  [{:keys [attr transcription grammaticality left-enclose right-enclose]
    :or {grammaticality "" left-enclose "" right-enclose ""}}]
  (when (some #{(utils/set-kw-ns-to-form attr)}
              @(re-frame/subscribe [::subs/visible-form-fields]))
    [re-com/h-box
     :src (at)
     :children
     [(when @(re-frame/subscribe [::subs/forms-labels-on?])
        [igt-label (-> attr attrs :label)])
      [re-com/box
       :max-width form-value-width
       :child
       (if (or (seq grammaticality) (seq transcription))
         (str left-enclose grammaticality transcription right-enclose)
         "")]]]))

(defn igt-translations [form-id translations]
  [re-com/h-box
   :src (at)
   :children
   [(when @(re-frame/subscribe [::subs/forms-labels-on?])
      [igt-label (-> attrs :translations :label)])
    [re-com/v-box
     :max-width form-value-width
     :children
     (for [{:keys [id grammaticality transcription]} translations]
       ^{:key (str form-id "-" id)}
       [re-com/box :child (str grammaticality transcription)])]]])

(defn secondary-scalar
  [attr value]
  (when (and (or (and (string? value) (seq value))
                 (and (not (string? value)) value))
             (some #{(utils/set-kw-ns-to-form attr)}
                   @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> attr attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (str value)]]]))

(defn id-string
  [id]
  [re-com/h-box
   :src (at)
   :children
   [[igt-label (or (-> :id attrs :label) "")]
    [re-com/box
     :max-width form-value-width
     :child
     [re-com/hyperlink
      :label (str id)
      :on-click (fn [& _]
                  (re-frame/dispatch
                   [::events/navigate
                    {:handler :form-page
                     :route-params
                     {:old @(re-frame/subscribe [::subs/old-slug])
                      :id id}}]))
      :tooltip "view this form"]]]])

(defn tags-as-string [tags]
  (when (and (seq tags)
             (some #{:form/tags} @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> :tags attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (str/join ", " (map :name tags))]]]))

(defn files-as-string [files]
  (when (and (seq files)
             (some #{:form/files}
                   @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> :files attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (str/join ", " (map :filename files))]]]))

(defn named-entity-as-string [attr entity]
  (when (and entity
             (some #{(utils/set-kw-ns-to-form attr)}
                   @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> attr attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (:name entity)]]]))

(def syntactic-category-as-string
  (partial named-entity-as-string :syntactic-category))

(def elicitation-method-as-string
  (partial named-entity-as-string :elicitation-method))

(defn source-as-string [{:as source :keys [author year]}]
  (when (and source
             (some #{:form/source}
                   @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> :source attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (str author " (" year ")")]]]))

(defn person-as-string [attr {:as person :keys [first-name last-name]}]
  (when (and person
             (some #{(utils/set-kw-ns-to-form attr)}
                   @(re-frame/subscribe [::subs/visible-form-fields])))
    [re-com/h-box
     :src (at)
     :children
     [[igt-label (or (-> attr attrs :label) "")]
      [re-com/box
       :max-width form-value-width
       :child (str first-name " " last-name)]]]))

(defn igt-form-igt
  [{:keys [grammaticality morpheme-break morpheme-gloss
           narrow-phonetic-transcription phonetic-transcription transcription
           translations uuid]}]
  [re-com/v-box
   :src (at)
   :children
   [(when (seq narrow-phonetic-transcription)
      [igt-transcription {:attr :narrow-phonetic-transcription
                          :transcription narrow-phonetic-transcription}])
    (when (seq phonetic-transcription)
      [igt-transcription {:attr :phonetic-transcription
                          :transcription phonetic-transcription}])
    [igt-transcription {:attr :transcription
                        :transcription transcription
                        :grammaticality grammaticality}]
    [igt-transcription {:attr :morpheme-break
                        :transcription morpheme-break
                        :left-enclose "/"
                        :right-enclose "/"}]
    [igt-transcription {:attr :morpheme-gloss
                        :transcription morpheme-gloss}]
    [igt-translations uuid translations]]])

(defn header-left [{form-id :uuid}]
  [re-com/h-box
   :src (at)
   :gap "5px"
   :size "auto"
   :children
   [[collapse-button form-id]
    [export-button form-id]]])

(defn header-right [{form-id :id}]
  [re-com/h-box
   :src (at)
   :gap "5px"
   :size "auto"
   :justify :end
   :children
   [[delete-form-button form-id]]])

(defn header [form]
  [re-com/h-box
   :src (at)
   :gap "5px"
   :class (styles/default)
   :children
   [[header-left form]
    [header-right form]]])

(defn form-export [export-string]
  [re-com/box
   :class (styles/export)
   :child [:pre {:style {:margin-bottom "0px"}} export-string]])

(defn igt-form-export-interface [{:as form form-id :uuid}]
  (when @(re-frame/subscribe [::subs/form-export-interface-visible? form-id])
    (let [{:keys [efn]} (form-exporter/export
                         @(re-frame/subscribe [::subs/form-export-format
                                               form-id]))
          export-string (efn form)]
      [re-com/v-box
       :class (styles/export-interface)
       :children
       [[re-com/h-box
         :gap "10px"
         :children [[form-export-select form-id]
                    [widgets/copy-button export-string "form export"]]]
        [form-export export-string]]])))

(defn igt-form-controls [{:as form form-id :uuid}]
  (when @(re-frame/subscribe [::subs/form-expanded? form-id])
    [re-com/v-box
     :class (styles/default)
     :children
     [[header form]
      [igt-form-export-interface form]]]))

(defn igt-form-secondary
  [{:keys [break-gloss-category comments datetime-entered datetime-modified
           date-elicited elicitation-method elicitor enterer files id modifier
           speaker-comments semantics source speaker status syntactic-category
           syntactic-category-string syntax tags uuid verifier]}]
  (when @(re-frame/subscribe [::subs/form-expanded? uuid])
    [re-com/v-box
     :src (at)
     :children
     [[secondary-scalar :syntactic-category-string syntactic-category-string]
      [secondary-scalar :break-gloss-category break-gloss-category]
      [secondary-scalar :comments comments]
      [secondary-scalar :speaker-comments speaker-comments]
      [tags-as-string tags]
      [syntactic-category-as-string syntactic-category]
      [elicitation-method-as-string elicitation-method]
      [secondary-scalar :date-elicited (utils/date->human-string date-elicited)]
      [person-as-string :speaker speaker]
      [person-as-string :elicitor elicitor]
      [person-as-string :verifier verifier]
      [person-as-string :enterer enterer]
      [secondary-scalar
       :datetime-entered
       (when datetime-entered
         (utils/datetime->human-string datetime-entered))]
      [person-as-string :modifier modifier]
      [secondary-scalar :datetime-modified (utils/datetime->human-string
                                            datetime-modified)]
      [files-as-string files]
      [source-as-string source]
      [secondary-scalar :status status]
      [secondary-scalar :semantics semantics]
      [secondary-scalar :syntax syntax]
      [secondary-scalar :uuid uuid]
      [id-string id]]]))

(defn igt-form [form-id]
  (let [form @(re-frame/subscribe [::subs/form-by-id form-id])
        expanded? @(re-frame/subscribe [::subs/form-expanded? form-id])]
    [re-com/v-box
     :src (at)
     :class (if expanded?
              (styles/form)
              (str (styles/form) " " (styles/actionable)))
     :attr {:on-click
            (fn [& _] (when-not expanded?
                        (re-frame/dispatch [::events/user-clicked-form form-id])))}
     :children
     [[igt-form-controls form]
      [igt-form-igt form]
      [igt-form-secondary form]]]))
