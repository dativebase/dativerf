(ns dativerf.models.form
  "Functionality for getting form entities from the in-memory db and validating
  forms."
  (:require [dativerf.utils :as utils]
            [dativerf.specs.form :as form-specs]))

(defn new-form [db]
  (-> (utils/select-keys-by-ns "new-form" db)
      utils/remove-namespaces-recursive
      (update :date-elicited
              (fn [de] (when de
                         (utils/goog-date-utc-date-time->mm-dd-yyyy-string de))))))

(def new-form-validation-message
  (str "A form must have at least one transcription-type value (transcription,"
       " morpheme break, or phonetic transcription) and it must have at least"
       " one translation."))

(defn string-of-max-len [len name]
  (str "A " name " can have at most " len " characters."))

(def new-form-field-validation-declarations
  (assoc
   (->> {:transcription 510
         :phonetic-transcription 510
         :narrow-phonetic-transcription 510
         :morpheme-break 510
         :morpheme-gloss 510
         :syntax 1023
         :semantics 1023}
        (map (juxt key (fn [[k v]] (string-of-max-len
                                    v (-> k name utils/kebab->space)))))
        (into {}))
   :date-elicited "A date elicited is a date string in the format 'MM/DD/YYYY'."
   :translations "A form requires at least one non-empty translation."))

(defn new-form-field-specific-validation-error-messages [new-form]
  (->> new-form
       form-specs/write-form-explain-data
       :cljs.spec.alpha/problems
       (map (comp first :path))
       (filter some?)
       set
       (map (juxt identity
                  (fn [k]
                    (get new-form-field-validation-declarations k
                         "The supplied value is invalid."))))
       (into {})))
