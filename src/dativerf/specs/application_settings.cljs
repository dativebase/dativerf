(ns dativerf.specs.application-settings
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]
            [dativerf.specs.orthography :as orthography]
            [dativerf.specs.user :as user]))

(def validation-values
  #{"None"
    "Warning"
    "Error"})
(s/def ::id ::common/id)
(s/def ::object-language-name (s/nilable ::common/string-lte-255))
(s/def ::object-language-id (s/nilable ::common/string-lte-3))
(s/def ::metalanguage-name (s/nilable ::common/string-lte-255))
(s/def ::metalanguage-id (s/nilable ::common/string-lte-3))
(s/def ::metalanguage-inventory (s/nilable string?))
(s/def ::orthographic-validation (s/nilable validation-values))
(s/def ::narrow-phonetic-inventory (s/nilable string?))
(s/def ::narrow-phonetic-validation (s/nilable validation-values))
(s/def ::broad-phonetic-inventory (s/nilable string?))
(s/def ::broad-phonetic-validation (s/nilable validation-values))
(s/def ::morpheme-break-is-orthographic (s/nilable boolean?))
(s/def ::morpheme-break-validation (s/nilable validation-values))
(s/def ::phonemic-inventory (s/nilable string?))
(s/def ::morpheme-delimiters (s/nilable ::common/string-lte-255))
(s/def ::punctuation (s/nilable string?))
(s/def ::grammaticalities (s/nilable ::common/string-lte-255))
(s/def ::datetime-modified ::common/datetime-string)
(s/def ::storage-orthography (s/nilable ::orthography/mini-orthography))
(s/def ::input-orthography (s/nilable ::orthography/mini-orthography))
(s/def ::output-orthography (s/nilable ::orthography/mini-orthography))
(s/def ::unrestricted-users ::user/mini-users)

(s/def ::application-settings (s/keys :req-un [::id
                                               ::object-language-name
                                               ::object-language-id
                                               ::metalanguage-name
                                               ::metalanguage-id
                                               ::metalanguage-inventory
                                               ::orthographic-validation
                                               ::narrow-phonetic-inventory
                                               ::narrow-phonetic-validation
                                               ::broad-phonetic-inventory
                                               ::broad-phonetic-validation
                                               ::morpheme-break-is-orthographic
                                               ::morpheme-break-validation
                                               ::phonemic-inventory
                                               ::morpheme-delimiters
                                               ::punctuation
                                               ::grammaticalities
                                               ::datetime-modified
                                               ::storage-orthography
                                               ::input-orthography
                                               ::output-orthography
                                               ::unrestricted-users]))

;; Write Application Settings

(s/def :write/morpheme-break-is-orthographic #{"true" "false"}) ;; weird, but true
(s/def :write/storage-orthography (s/nilable ::orthography/id))
(s/def :write/input-orthography (s/nilable ::orthography/id))
(s/def :write/output-orthography (s/nilable ::orthography/id))
(s/def :write/unrestricted-users (s/coll-of ::common/id :distinct true))

(s/def ::write-application-settings (s/keys :req-un [::object-language-name
                                                     ::object-language-id
                                                     ::metalanguage-name
                                                     ::metalanguage-id
                                                     ::metalanguage-inventory
                                                     ::orthographic-validation
                                                     ::narrow-phonetic-inventory
                                                     ::narrow-phonetic-validation
                                                     ::broad-phonetic-inventory
                                                     ::broad-phonetic-validation
                                                     ;; :write/morpheme-break-is-orthographic
                                                     ::morpheme-break-is-orthographic
                                                     ::morpheme-break-validation
                                                     ::phonemic-inventory
                                                     ::morpheme-delimiters
                                                     ::punctuation
                                                     ::grammaticalities
                                                     :write/storage-orthography
                                                     :write/input-orthography
                                                     :write/output-orthography
                                                     :write/unrestricted-users]))

;; New Application Settings Data
;;
;; This is a spec for the data needed to create a new application settings, or
;; update an existing one.
;; WARNING: I was adding ::language/languages to this spec previously. However,
;; that had serious performance costs.
(s/def ::users ::user/mini-users)
(s/def ::orthographies ::orthography/mini-orthographies)
(s/def ::new-data (s/keys :opt-un [::orthographies
                                   ::users]))

(def application-settings-valid? (partial s/valid? ::application-settings))
(def application-settings-explain-data (partial s/explain-data ::application-settings))

(def write-application-settings-valid?
  (partial s/valid? ::write-application-settings))
(def write-application-settings-explain-data
  (partial s/explain-data ::write-application-settings))

(def new-data-valid? (partial s/valid? ::new-data))
(def new-data-explain-data (partial s/explain-data ::new-data))

;; NOTE: all seeming app settings updates are actually creates, ie POST requests.
