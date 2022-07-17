(ns dativerf.models.form
  "Functionality for getting form entities from the in-memory db and validating
  forms."
  (:require [dativerf.specs.form :as spec]
            [dativerf.utils :as utils]))

(def metadata
  {:narrow-phonetic-transcription
   {:label "narr. phon. transcr."
    :description "A narrow phonetic transcription, probably in IPA."}
   :phonetic-transcription
   {:label "phon. transcr."
    :description "A phonetic transcription, probably in IPA."}
   :transcription
   {:description "A transcription, probably orthographic."}
   :morpheme-break
   {:description "A sequence of morpheme shapes and delimiters. The OLD assumes
              phonemic shapes (e.g., “in-perfect”), but phonetic (i.e.,
              allomorphic, e.g., “im-perfect”) ones are ok."}
   :morpheme-gloss
   {:description "A sequence of morpheme glosses and delimiters, isomorphic to
             the morpheme break sequence, e.g., “NEG-parfait”."}
   :translations
   {:description "One or more translations for the form. Each translation may have
              its own grammaticality/acceptibility specification."}
   :comments
   {:description "General-purpose field for notes and commentary about the form."}
   :speaker-comments
   {:description "Field specifically for comments about the form made by the
              speaker/consultant."}
   :elicitation-method
   {:description "How the form was elicited. Examples: “volunteered”, “judged
              elicitor’s utterance”, “translation task”, etc."}
   :tags
   {:description "Tags for categorizing your forms."}
   :syntactic-category
   {:description "The category (syntactic and/or morphological) of the form."}
   :date-elicited
   {:description "The date this form was elicited"}
   :speaker
   {:description "The speaker (consultant) who produced or judged the form."}
   :elicitor
   {:description "The linguistic fieldworker who elicited the form with the help
              of the consultant."}
   :verifier
   {:description "The user who has verified the reliability/accuracy of this form."}
   :source
   {:description "The textual source (e.g., research paper, text collection, book
              of learning materials) from which the form was drawn, if
              applicable."}
   :files
   {:description "Digital files (e.g., audio, video, image or text) that are
              associated to this form."}
   :syntax
   {:description "A syntactic phrase structure representation in some kind of
              string-based format."}
   :semantics
   {:description "A semantic representation of the meaning of the form in some
              string-based format."}
   :status
   {:description "The status of the form: “tested” for data that have been
              elicited/tested/verified with a consultant or “requires testing”
              for data that are posited and still need testing/elicitation."}})

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
       spec/write-form-explain-data
       :cljs.spec.alpha/problems
       (map (comp first :path))
       (filter some?)
       set
       (map (juxt identity
                  (fn [k]
                    (get new-form-field-validation-declarations k
                         "The supplied value is invalid."))))
       (into {})))

(defn parse-form [form]
  (-> form
      (update :date-elicited utils/date-string?)
      (update :datetime-entered utils/datetime-string?)
      (update :datetime-modified utils/datetime-string?)
      (update :uuid uuid)))

(defn process-forms-new-response
  "Validate a GET forms/new response fetched from the OLD."
  [resources]
  (let [data (reduce
              (fn [agg [extractor key]]
                (if-let [d (extractor resources)]
                  (assoc agg key (utils/->kebab-case-recursive d))
                  agg))
              {}
              [[:elicitation_methods :elicitation-methods]
               [:grammaticalities :grammaticalities]
               [:sources :sources]
               [:speakers :speakers]
               [:syntactic_categories :syntactic-categories]
               [:tags :tags]
               [:users :users]])]
    (when-not (spec/new-data-valid? data)
      (throw (ex-info "Invalid New Form Data"
                      {:new-data data
                       :error-code :invalid-new-form-data
                       :explain-data (spec/new-data-explain-data data)})))
    data))

(def editable-keys
  [:comments
   :date-elicited
   :elicitation-method
   :elicitor
   :files
   :morpheme-break
   :morpheme-gloss
   :narrow-phonetic-transcription
   :phonetic-transcription
   :semantics
   :source
   :speaker
   :speaker-comments
   :status
   :syntactic-category
   :syntax
   :tags
   :transcription
   :translations
   :verifier])
