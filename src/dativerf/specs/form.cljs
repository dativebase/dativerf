(ns dativerf.specs.form
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators]
            [clojure.string :as str]
            [dativerf.db :as db]
            [dativerf.specs.common :as common]
            [dativerf.specs.elicitation-method :as elicitation-method]
            [dativerf.specs.file :as file]
            [dativerf.specs.source :as source]
            [dativerf.specs.speaker :as speaker]
            [dativerf.specs.syntactic-category :as syntactic-category]
            [dativerf.specs.tag :as tag]
            [dativerf.specs.user :as user]
            [dativerf.utils :as utils]
            [goog.string :as gstring]
            [goog.string.format]))

;; TODO: this ns is called form but there are specs for all types of resources
;; in here.

(defn uuid-string? [x]
  (and (string? x)
       (re-find #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
                x)))
(s/def ::uuid-string uuid-string?)

(s/def :translation/id ::common/id)
(s/def :translation/transcription string?)
(s/def :translation/grammaticality string?)
(s/def ::translation (s/keys :req-un [:translation/id
                                      :translation/transcription
                                      :translation/grammaticality]))
(s/def ::translations (s/coll-of ::translation))

;; Form spec

(s/def :form/tags ::tag/tags)
(s/def :form/date-elicited (s/nilable ::common/date-string))
(s/def :form/narrow-phonetic-transcription string?)
(s/def :form/enterer (s/nilable ::user/mini-user))
;; TODO: datetime-entered should not be nilable, but OLDs do have forms with a
;; nil value here...
(s/def :form/datetime-entered (s/nilable ::common/datetime-string))
(s/def :form/verifier (s/nilable ::user/mini-user))
(s/def :form/break-gloss-category string?)
(s/def :form/modifier (s/nilable ::user/mini-user))
(s/def :form/syntactic-category (s/nilable ::syntactic-category/mini-syntactic-category))
(s/def :form/speaker-comments string?)
;; TODO: better spec for morpheme-gloss-ids
(s/def :form/morpheme-gloss-ids (s/nilable (s/coll-of any?)))
(s/def :form/semantics string?)
(s/def :form/comments string?)
(s/def :form/source (s/nilable ::source/mini-source))
(s/def :form/syntactic-category-string string?)
;; TODO: dynamic validation based on app settings
(s/def :form/grammaticality string?)
(s/def :form/status string?) ;; TODO: closed class?
(s/def :form/syntax string?)
(s/def :form/id ::common/id)
(s/def :form/morpheme-gloss (s/nilable string?))
(s/def :form/files ::file/mini-files)
(s/def :form/elicitation-method (s/nilable ::elicitation-method/mini-elicitation-method))
(s/def :form/datetime-modified ::common/datetime-string)
(s/def :form/uuid ::uuid-string)
(s/def :form/morpheme-break string?)
(s/def :form/speaker (s/nilable ::speaker/mini-speaker))
;; TODO: better spec for morpheme-break-ids
(s/def :form/morpheme-break-ids (s/nilable (s/coll-of any?)))
(s/def :form/phonetic-transcription string?)
(s/def :form/transcription string?)
(s/def :form/elicitor (s/nilable ::user/mini-user))
(s/def :form/translations ::translations)

(s/def ::form (s/keys :req-un [:form/tags
                               :form/date-elicited
                               :form/narrow-phonetic-transcription
                               :form/enterer
                               :form/datetime-entered
                               :form/verifier
                               :form/break-gloss-category
                               :form/modifier
                               :form/syntactic-category
                               :form/speaker-comments
                               :form/morpheme-gloss-ids
                               :form/semantics
                               :form/comments
                               :form/source
                               :form/syntactic-category-string
                               :form/grammaticality
                               :form/status
                               :form/syntax
                               :form/id
                               :form/morpheme-gloss
                               :form/files
                               :form/elicitation-method
                               :form/datetime-modified
                               :form/uuid
                               :form/morpheme-break
                               :form/speaker
                               :form/morpheme-break-ids
                               :form/phonetic-transcription
                               :form/transcription
                               :form/elicitor
                               :form/translations]))
(s/def ::forms (s/coll-of ::form))

(defn form-explain-data [form]
  (s/explain-data ::form form))

;; Write Form
;;
;; This is what a form map must look like on a write (POST or PUT) request.
;; See the OLD's validation via FormSchema at
;; https://github.com/dativebase/old-pyramid/blob/master/old/lib/schemata.py#L337
;;
;; - TODO: implement client-side validation of form transcription-type values
;;   based on the state of the Orthographies resource and the Input Validation
;;   settings of the current OLD's application settings.

(s/def :write-translation/transcription string?)
(s/def :write-translation/grammaticality
  (s/with-gen string?
    #(gen/elements ["" "*" "?" "#"])))
(s/def :write-form/translation
  (s/keys :req-un [:write-translation/transcription
                   :write-translation/grammaticality]))
;; :translations coll must contain at least one translation and at least one
;; translation with a non-empty transcription value.
(s/def :write-form/translations
  (s/and (s/coll-of :write-form/translation :min-count 1)
         (fn [ts] (> (->> ts
                          (filter (fn [{:keys [transcription]}]
                                    ((complement str/blank?)
                                     (str/trim transcription))))
                          count)
                     0))))

;; Write date-elicited values are date strings of format "MM/DD/YYYY"
(s/def :write-form/date-elicited
  (s/with-gen
    (s/nilable
     (s/and string?
            (partial re-find
                     ;; TODO: maybe not the best idea to use a regex here. Note
                     ;; that it accepts invalid dates like "02/31/2022".
                     #"^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/[0-9]{4}$")))
    #(gen/fmap (fn [[m d y]]
                 (str (gstring/format "%02d" m)
                      "/"
                      (gstring/format "%02d" d)
                      "/"
                      (gstring/format "%04d" y)))
               (gen/tuple (gen/choose 1 12) (gen/choose 1 31) (gen/choose 1800 2200)))))

(defn- max-length [l string] (<= (count string) l))

(s/def :write-form/grammaticality
  (s/with-gen string?
    #(gen/elements ["" "*" "?" "#"])))

(s/def :write-form/transcription (s/and string? (partial max-length 510)))
(s/def :write-form/phonetic-transcription (s/and string? (partial max-length 510)))
(s/def :write-form/narrow-phonetic-transcription (s/and string? (partial max-length 510)))
(s/def :write-form/morpheme-break (s/and string? (partial max-length 510)))
(s/def :write-form/morpheme-gloss (s/and string? (partial max-length 510)))
(s/def :write-form/comments string?)
(s/def :write-form/speaker-comments string?)
(s/def :write-form/syntax (s/and string? (partial max-length 1023)))
(s/def :write-form/semantics (s/and string? (partial max-length 1023)))
(s/def :write-form/status #{"tested" "requires testing"})
(s/def :write-form/elicitation-method (s/nilable ::common/id))
(s/def :write-form/syntactic-category (s/nilable ::syntactic-category/id))
(s/def :write-form/speaker (s/nilable ::speaker/id))
(s/def :write-form/elicitor (s/nilable ::user/id))
(s/def :write-form/verifier (s/nilable ::user/id))
(s/def :write-form/source (s/nilable ::source/id))
(s/def :write-form/tags (s/coll-of ::tag/id :distinct true))
(s/def :write-form/files (s/coll-of ::file/id :distinct true))

(defn- one-non-empty-transcription-type-value [form]
  (> (->> (select-keys form
                       [:transcription :morpheme-break :phonetic-transcription
                        :narrow-phonetic-transcription])
          vals
          (filter (fn [v] ((complement str/blank?) (str/trim v))))
          count)
     0))

(s/def ::write-form
  (s/and
   (s/keys :req-un [:write-form/narrow-phonetic-transcription
                    :write-form/phonetic-transcription
                    :write-form/grammaticality
                    :write-form/transcription
                    :write-form/morpheme-break
                    :write-form/morpheme-gloss
                    :write-form/translations
                    :write-form/comments
                    :write-form/speaker-comments
                    :write-form/syntax
                    :write-form/semantics
                    :write-form/status
                    :write-form/elicitation-method
                    :write-form/syntactic-category
                    :write-form/speaker
                    :write-form/elicitor
                    :write-form/verifier
                    :write-form/source
                    :write-form/tags
                    :write-form/files
                    :write-form/date-elicited])
   one-non-empty-transcription-type-value))

(s/def :new-form/elicitation-methods ::elicitation-method/mini-elicitation-methods)
(s/def :new-form/grammaticality string?)
(s/def :new-form/grammaticalities (s/coll-of :new-form/grammaticality))
(s/def :new-form/sources ::source/mini-sources)
(s/def :new-form/speakers ::speaker/mini-speakers)
(s/def :new-form/syntactic-categories ::syntactic-category/mini-syntactic-categories)
(s/def :new-form/tags ::tag/mini-tags)
(s/def :new-form/users ::user/mini-users)
(s/def :new-form/new-data (s/keys :opt-un [:new-form/elicitation-methods
                                           :new-form/grammaticalities
                                           :new-form/sources
                                           :new-form/speakers
                                           :new-form/syntactic-categories
                                           :new-form/tags
                                           :new-form/users]))

(def new-data-valid? (partial s/valid? :new-form/new-data))
(def new-data-explain-data (partial s/explain-data :new-form/new-data))

(def write-form-valid? (partial s/valid? ::write-form))
(def write-form-explain-data (partial s/explain-data ::write-form))

(comment

  ;; Generate some write forms
  (gen/sample (s/gen ::write-form))

  ;; A valid form is recognized as valid
  (nil?
   (s/explain-data
    ::write-form
    (merge (utils/remove-namespaces-recursive db/default-new-form-state)
           {:transcription "a"
            :translations
            [{:transcription "b" :grammaticality ""}]})))

  ;; An valid form has explanatory data
  (s/explain-data
   ::write-form
   (merge (utils/remove-namespaces-recursive db/default-new-form-state)
          {:transcription "" ;; need a non-empty transcription-type form
           :translations
           [{:transcription "b" :grammaticality ""}]}))

  (s/explain-data
   ::write-form
   (merge (utils/remove-namespaces-recursive db/default-new-form-state)
          {:transcription 2
           :translations
           [{:transcription "b" :grammaticality ""}]}))

)
