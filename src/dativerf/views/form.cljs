(ns dativerf.views.form
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.specs.form :as form-specs]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]
            [reagent.core :as reagent]))

;; Buttons

(defn x-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-plus"
   :size :smaller
   :tooltip "x"
   :disabled? true])

;; End Buttons

(def igt-keys
  [:narrow-phonetic-transcription
   :phonetic-transcription
   :transcription
   :morpheme-break
   :morpheme-gloss
   :translations])

;; :grammaticality
;; :morpheme-break-ids
;; :morpheme-gloss-ids

(def metadata-keys
  [:syntactic-category-string
   :break-gloss-category
   :date-elicited
   :comments
   :speaker-comments
   :tags
   :syntactic-category
   :speaker
   :enterer
   :datetime-entered
   :modifier
   :datetime-modified
   :verifier
   :elicitation-method
   :elicitor
   :source
   :files
   :status
   :syntax
   :semantics
   :uuid
   :id])

(def form-rows
  [{:key :transcription :type :string}
   {:key :morpheme-break :type :string}
   {:key :morpheme-gloss :type :string}
   {:key :translations :type :coll-of-translations}])

(def attrs
  {:transcription
   {:label "transcription"
    :tooltip "A transcription, probably orthographic."}
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
   :translations
   {:label "translations"}})

(defn igt-label [label]
  [re-com/box
   :class (styles/attr-label)
   :justify :end
   :padding "0 1em 0 0"
   :child label])

(defn igt-transcription
  [{:keys [attr transcription grammaticality left-enclose right-enclose]
    :or {grammaticality "" left-enclose "" right-enclose ""}}]
  [re-com/h-box
   :src (at)
   :children
   [[igt-label (-> attr attrs :label)]
    [re-com/box
     :max-width "500px"
     :child
     (if (or (seq grammaticality) (seq transcription))
       (str left-enclose grammaticality transcription right-enclose)
       "")]]])

(defn igt-translations [form-id translations]
  [re-com/h-box
   :src (at)
   :children
   [[igt-label (-> attrs :translations :label)]
    [re-com/v-box
     :max-width "500px"
     :children
     (for [{:keys [id grammaticality transcription]} translations]
       ^{:key (str form-id "-" id)}
       [re-com/box :child (str grammaticality transcription)])]]])

(defn igt-form [form-id]
  (let [{:keys [grammaticality morpheme-break morpheme-gloss transcription
                translations]}
        (form-specs/parse-form @(re-frame/subscribe [::subs/form-by-id
                                                     form-id]))]
    [re-com/v-box
     :src (at)
     :class (styles/objlang)
     :children
     [[igt-transcription {:attr :transcription
                          :transcription transcription
                          :grammaticality grammaticality}]
      [igt-transcription {:attr :morpheme-break
                          :transcription morpheme-break
                          :left-enclose "/"
                          :right-enclose "/"}]
      [igt-transcription {:attr :morpheme-gloss
                          :transcription morpheme-gloss}]
      [igt-translations form-id translations]]]))
