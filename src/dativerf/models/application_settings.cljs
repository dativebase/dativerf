(ns dativerf.models.application-settings
  (:require [dativerf.specs.application-settings :as spec]
            [dativerf.utils :as utils]
            [clojure.string :as str]))

;; Metadata about Application Settings Fields

(def ^:private validation-meaning
  (str "'None' means no validation."
       " 'Warning' means a warning is generated when a user tries invalid"
       " input."
       " 'Error' means invalid input is forbidden."))

(defn- inventory-tooltip
  ([field] (inventory-tooltip field ""))
  ([field postamble]
   (str "A comma-delimited list of graphemes that should be used when entering"
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

(def ^:private object-language-description
  "is being documented and analyzed by means of this OLD web service")

(def ^:private metalanguage-description
  "is being used to translate, document and analyze the object language")

(def settings-metadata
  {:broad-phonetic-inventory
   {:description (inventory-tooltip "phonetic transcription")}
   :broad-phonetic-validation
   {:description (str "How to validate user input in the 'phonetic"
                      " transcription' field. " validation-meaning)}
   :datetime-modified
   {:description (str "When these settings were last modified.")}
   :grammaticalities
   {:description (str "A comma-delimited list of characters that will define the"
                      " options in the grammaticality fields. Example:"
                      " \u201c*,?,#\u201d.")}
   :metalanguage-id
   {:description (language-id-tooltip metalanguage-description)}
   :metalanguage-name
   {:description (language-name-tooltip metalanguage-description)}
   :morpheme-break-is-orthographic
   {:label "morph. break orthographic?"
    :description (str "Morpheme break is orthographic? If set to true, this means"
                      " that the morpheme break field should be validated against"
                      " the storage orthography. If set to false, it means that it"
                      " should be validated against the phonemic inventory.")}
   :morpheme-break-validation
   {:description (str "How to validate user input in the 'morpheme break' field. "
                      validation-meaning)}
   :morpheme-delimiters
   {:description (str "A comma-delimited list of delimiter"
                      " characters that should be used to separate morphemes in the"
                      " morpheme break field and morpheme glosses in the morpheme"
                      " gloss field.")}
   :narrow-phonetic-inventory
   {:description (inventory-tooltip "narrow phonetic transcription")}
   :narrow-phonetic-validation
   {:description (str "How to validate user input in the 'narrow phonetic"
                      " transcription' field. " validation-meaning)}
   :object-language-id
   {:description (language-id-tooltip object-language-description)}
   :object-language-name
   {:description (language-name-tooltip object-language-description)}
   :orthographic-validation
   {:description (str "How to validate user input in the 'transcription' field. "
                      validation-meaning)}
   :phonemic-inventory
   {:description (inventory-tooltip
                  "morpheme break"
                  " (assuming 'morpheme break is orthographic' is set to false)")}
   :punctuation
   {:description (str "A string of punctuation characters that should define, along"
                      " with the graphemes in the storage orthography, the licit"
                      " strings in the transcription field.")}
   :storage-orthography
   {:description (str "The orthography that transcription values should be stored"
                      " in. This orthography may affect how orthographic validation"
                      " works and/or how orthography conversion works.")}
   :unrestricted-users
   {:description (str "A list of users that the OLD server considers to be"
                      " \u201cunrestricted\u201d. These users are able to access"
                      " data that has been tagged with the \u201crestricted\u201d"
                      " tag.")}})

(defn parse-inventory
  ([inventory] (parse-inventory inventory {}))
  ([inventory names]
   (mapv (fn [graph]
           (let [graph (utils/remove-enclosing-brackets graph)]
             {:graph graph
              :graphemes (utils/unicode-inspect graph names)}))
         (utils/parse-comma-delimited-string inventory))))

(defn parse-punctuation
  ([punctuation] (parse-punctuation punctuation {}))
  ([punctuation names]
   (mapv (fn [character]
           {:graph character
            :graphemes (utils/unicode-inspect character names)})
         (str/replace punctuation #"\s" ""))))

(defn process-response
  [preprocessor response]
  (let [current-application-settings (-> response
                                         preprocessor
                                         utils/->kebab-case-recursive)]
    (when-not (spec/application-settings-valid? current-application-settings)
      (throw (ex-info "Invalid Application Settings"
                      {:application-settings current-application-settings
                       :error-code :invalid-application-settings
                       :explain-data (spec/application-settings-explain-data
                                      current-application-settings)})))
    (update current-application-settings
            :datetime-modified
            utils/parse-datetime-string)))

(def process-fetch-response (partial process-response last))

(def process-create-response (partial process-response identity))

(defn process-settings-new-response
  "Validate a GET applicationsettings/new response fetched from the OLD.
  The tricky part here is that languages is very large and recursively
  kebab-casing it takes a long time (~4s in my experience.) Therefore, we
  convert languages to a map from Id strings to Ref_Name strings."
  [{:keys [users orthographies languages]}]
  (let [data (cond-> {}
               users
               (assoc :users (utils/->kebab-case-recursive users))
               orthographies
               (assoc :orthographies (utils/->kebab-case-recursive
                                      orthographies))
               languages
               (assoc :languages
                      (->> languages
                           (map (juxt :Id :Ref_Name))
                           (into {}))))]
    (when-not (spec/new-data-valid? data)
      (throw (ex-info "Invalid New Application Settings Data"
                      {:new-data data
                       :error-code :invalid-new-application-settings-data
                       :explain-data (spec/new-data-explain-data data)})))
    data))

(defn- maybe-orthography-id [orthography] (some-> orthography :id))

(def editable-keys
  [:broad-phonetic-inventory
   :broad-phonetic-validation
   :grammaticalities
   :input-orthography
   :metalanguage-id
   :metalanguage-inventory
   :metalanguage-name
   :morpheme-break-is-orthographic
   :morpheme-break-validation
   :morpheme-delimiters
   :narrow-phonetic-inventory
   :narrow-phonetic-validation
   :object-language-id
   :object-language-name
   :orthographic-validation
   :output-orthography
   :phonemic-inventory
   :punctuation
   :storage-orthography
   :unrestricted-users])

(defn read-settings->write-settings [read-settings]
  (let [write-settings
        (-> read-settings
            (select-keys editable-keys)
            (update :orthographic-validation spec/validation-values)
            (update :narrow-phonetic-validation spec/validation-values)
            (update :broad-phonetic-validation spec/validation-values)
            (update :morpheme-break-validation spec/validation-values)
            (update :storage-orthography maybe-orthography-id)
            (update :input-orthography maybe-orthography-id)
            (update :output-orthography maybe-orthography-id)
            (update :unrestricted-users (fn [users] (set (map :id users)))))]
    (when-not (spec/write-application-settings-valid? write-settings)
      (throw (ex-info "Invalid Write Application Settings"
                      {:application-settings write-settings
                       :error-code :invalid-application-settings
                       :explain-data (spec/write-application-settings-explain-data
                                      write-settings)})))
    write-settings))

(defn edited-setting [db k]
  (get-in db [:settings/edited-settings k]
          ;; default to the most recent value fetched from the OLD
          (-> db
              (get-in [:old-states (:old db) :application-settings])
              read-settings->write-settings
              k)))

(defn edited-settings [db]
  (->> editable-keys
       (map (juxt identity (partial edited-setting db)))
       (into {})))

(defn string-of-max-len [len name]
  (str "A " name " can have at most " len " characters."))

(def edited-settings-field-validation-declarations
  (assoc
   (->> {:object-language-name 255
         :object-language-id 3
         :metalanguage-name 255
         :metalanguage-id 3}
        (map (juxt key (fn [[k v]] (string-of-max-len
                                    v (-> k name utils/kebab->space)))))
        (into {}))
   :unrestricted-users "Unrestricted users is a (possibly empty) set of user IDs."))

(defn edited-settings-field-specific-validation-error-messages [edited-settings]
  (cljs.pprint/pprint (->> edited-settings spec/write-application-settings-explain-data))
  (->> edited-settings
       spec/write-application-settings-explain-data
       :cljs.spec.alpha/problems
       (map (comp first :path))
       (filter some?)
       set
       (map (juxt identity
                  (fn [k]
                    (get edited-settings-field-validation-declarations k
                         "The supplied value is invalid."))))
       (into {})))
