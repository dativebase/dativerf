(ns dativerf.views.form.exports
  "Define export functions for single forms here. We may eventually want to
  create sub-namespaces such as dativerf.views.form.exports.latex etc.
  To create a new export, add a new map to the exports vec with :id, :label and
  :efn keys. The value of :efn should be an export function. It takes a form and
  returns a string."
  (:require [camel-snake-kebab.core :as csk]
            [dativerf.utils :as utils]
            [clojure.string :as str]))

;; Plain text export
;; NOTE: we probably want a better plain-text export than this. This is just an
;; initial example.

(def plain-text-export-fields
  [{:label "transcription"
    :getter (fn [{:keys [transcription grammaticality]}]
              (str grammaticality transcription))}
   {:label "morpheme break"
    :getter :morpheme-break}
   {:label "morpheme gloss"
    :getter :morpheme-gloss}
   {:label "translations"
    :getter (fn [{:keys [translations]}]
              (->> translations
                   (map (fn [{:keys [transcription grammaticality]}]
                          (str \' grammaticality transcription \')))
                   (str/join ", ")))}])

(defn plain-text-export [form]
  (->> plain-text-export-fields
       (map (fn [{:keys [label getter]}]
              (str label ": " (getter form))))
       (str/join "\n")))

;; JSON export

(defn json-export [form]
  (.stringify js/JSON (clj->js
                       (-> form
                           (dissoc :dative/fetched-at)
                           (update :uuid str)
                           utils/->snake-case-recursive))
              nil
              2))

;; API

(def exports
  [{:id :plain-text
    :label "Plain Text"
    :efn plain-text-export}
   {:id :json
    :label "JSON"
    :efn json-export}])

(defn export [export-id]
  (first (for [{:as e :keys [id]} exports
               :when (= export-id id)] e)))
