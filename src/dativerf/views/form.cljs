(ns dativerf.views.form
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.specs.form :as form-specs]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]))

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

(defn igt-form [form-id]
  (let [form (form-specs/parse-form
              @(re-frame/subscribe [::subs/form-by-id form-id]))]
    [re-com/v-box
     :src (at)
     :children
     (for [{:as row :keys [key]} form-rows]
       ^{:key key} [widgets/key-value-row
                    (utils/kebab->space (name key))
                    (assoc row :value (key form))])]))
