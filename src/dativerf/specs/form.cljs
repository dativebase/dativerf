(ns dativerf.specs.form
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [cljs-time.format :as timef]))

;; General specs and specs that should be moved to their own namespaces

(s/def :int/id pos-int?)

(defn parse-date-string [x]
  (try (timef/parse (timef/formatters :date) x)
       (catch js/Error _)))

(defn date-string? [^string x]
  (and (string? x)
       (parse-date-string x)))

(s/def ::date-string date-string?)

(defn parse-datetime-string
  "Parse a datetime string like 2022-02-23T18:27:49.628337. To do this, we must
  remove the last 3 digits, reducing the microsecond precision to millisecond
  precision."
  [x]
  (try (timef/parse
        (timef/formatters :date-hour-minute-second-ms)
        (str/replace x #"\.(\d{3})\d{3}$" ".$1"))
       (catch js/Error _)))

(defn datetime-string? [^string x]
  (and (string? x)
       (parse-datetime-string x)))

(s/def ::datetime-string datetime-string?)

(s/def :user/id :int/id)
(s/def :user/first-name string?)
(s/def :user/last-name string?)
(s/def :user/role #{"administrator" "contributor" "viewer"})
(s/def ::user (s/keys :req-un [:user/id
                               :user/first-name
                               :user/last-name
                               :user/role]))

(s/def :tag/id :int/id)
(s/def :tag/name string?)
(s/def :tag/description string?)
(s/def :tag/datetime-modified ::datetime-string)
(s/def ::tag (s/keys :req-un [:tag/id
                              :tag/name
                              :tag/description
                              :tag/datetime-modified]))
(s/def ::tags (s/coll-of ::tag))

(s/def :syntactic-category/id :int/id)
(s/def :syntactic-category/name string?)
(s/def ::syntactic-category (s/keys :req-un [:syntactic-category/id
                                             :syntactic-category/name]))
(s/def ::syntactic-categories (s/coll-of ::syntactic-category))

(s/def :source/id :int/id) ;; 3,
(s/def :source/institution string?) ;; "",
(s/def :source/publisher string?) ;; "Toronto: University of Toronto Press",
(s/def :source/key string?) ;; "frantz1995",
(s/def :source/school string?) ;; "",
(s/def :source/type string?) ;; "book",
(s/def :source/journal string?) ;; "",
(s/def :source/title string?) ;; "Blackfoot Dictionary of Stems, Roots and Affixes",
(s/def :source/pages string?) ;; "",
(s/def :source/note string?) ;; "",
(s/def :source/author string?) ;; "Frantz, D G and Russell, N J",
(s/def :source/chapter string?) ;; "",
(s/def :source/year int?) ;; 1995,
(s/def :source/crossref string?) ;; "",
(s/def :source/editor string?) ;; "",
(s/def :source/booktitle string?) ;; ""}
(s/def ::source (s/keys :req-un
                        [:source/institution
                         :source/publisher
                         :source/key
                         :source/school
                         :source/type
                         :source/journal
                         :source/title
                         :source/pages
                         :source/note
                         :source/author
                         :source/chapter
                         :source/year
                         :source/crossref
                         :source/id
                         :source/editor
                         :source/booktitle]))

(s/def :file/id :int/id)
(s/def :file/name string?)
(s/def :file/filename string?)
(s/def :file/mime-type string?) ;; TODO: closed set of types
(s/def :file/size nat-int?)
(s/def :file/url (s/nilable string?))
(s/def :file/lossy-filename (s/nilable string?))

;; TODO: there are three distinct types of files, each with its own spec
(s/def ::file (s/keys :req-un [:file/id
                               :file/name
                               :file/filename
                               :file/mime-type
                               :file/size
                               :file/url
                               :file/lossy-filename]))
(s/def ::files (s/coll-of ::file))

(s/def :elicitation-method/id :int/id)
(s/def :elicitation-method/name string?)
(s/def ::elicitation-method (s/keys :req-un [:elicitation-method/id
                                             :elicitation-method/name]))
(s/def ::elicitation-methods (s/coll-of ::elicitation-method))

(defn uuid-string? [x]
  (and (string? x)
       (re-find #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
                x)))
(s/def ::uuid-string uuid-string?)

(s/def :speaker/id :int/id)
(s/def :speaker/first-name string?)
(s/def :speaker/last-name string?)
(s/def :speaker/dialect string?)
(s/def ::speaker (s/keys :req-un [:speaker/id
                                  :speaker/first-name
                                  :speaker/last-name
                                  :speaker/dialect]))
(s/def ::speakers (s/coll-of ::speaker))

(s/def :translation/id :int/id)
(s/def :translation/transcription string?)
(s/def :translation/grammaticality string?)
(s/def ::translation (s/keys :req-un [:translation/id
                                      :translation/transcription
                                      :translation/grammaticality]))
(s/def ::translations (s/coll-of ::translation))

;; Form spec

(s/def :form/tags ::tags)
(s/def :form/date-elicited (s/nilable ::date-string))
(s/def :form/narrow-phonetic-transcription string?)
(s/def :form/enterer (s/nilable ::user))
;; TODO: datetime-entered should not be nilable, but OLDs do have forms with a
;; nil value here...
(s/def :form/datetime-entered (s/nilable ::datetime-string))
(s/def :form/verifier (s/nilable ::user))
(s/def :form/break-gloss-category string?)
(s/def :form/modifier (s/nilable ::user))
(s/def :form/syntactic-category (s/nilable ::syntactic-category))
(s/def :form/speaker-comments string?)
;; TODO: better spec for morpheme-gloss-ids
(s/def :form/morpheme-gloss-ids (s/nilable (s/coll-of any?)))
(s/def :form/semantics string?)
(s/def :form/comments string?)
(s/def :form/source (s/nilable ::source))
(s/def :form/syntactic-category-string string?)
;; TODO: dynamic validation based on app settings
(s/def :form/grammaticality string?)
(s/def :form/status string?) ;; TODO: closed class?
(s/def :form/syntax string?)
(s/def :form/id :int/id)
(s/def :form/morpheme-gloss (s/nilable string?))
(s/def :form/files ::files)
(s/def :form/elicitation-method (s/nilable ::elicitation-method))
(s/def :form/datetime-modified ::datetime-string)
(s/def :form/uuid ::uuid-string)
(s/def :form/morpheme-break string?)
(s/def :form/speaker (s/nilable ::speaker))
;; TODO: better spec for morpheme-break-ids
(s/def :form/morpheme-break-ids (s/nilable (s/coll-of any?)))
(s/def :form/phonetic-transcription string?)
(s/def :form/transcription string?)
(s/def :form/elicitor (s/nilable ::user))
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

(defn parse-form [form]
  (-> form
      (update :date-elicited date-string?)
      (update :datetime-entered datetime-string?)
      (update :datetime-modified datetime-string?)
      (update :uuid uuid)))
