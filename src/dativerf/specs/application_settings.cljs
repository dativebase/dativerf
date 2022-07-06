(ns dativerf.specs.application-settings
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]
            [dativerf.specs.language :as language]
            [dativerf.specs.orthography :as orthography]
            [dativerf.specs.user :as user]))

(def validation-values
  {"None" :none
   "Warning" :warning
   "Error" :error})
(def read-validation-value? (set (keys validation-values)))
(def write-validation-value? (set (vals validation-values)))

(s/def ::id ::common/id)
(s/def ::object-language-name (partial common/string-max-len? 255))
(s/def ::object-language-id (partial common/string-max-len? 3))
(s/def ::metalanguage-name (partial common/string-max-len? 255))
(s/def ::metalanguage-id (partial common/string-max-len? 3))
(s/def ::metalanguage-inventory string?)
(s/def ::orthographic-validation read-validation-value?)
(s/def ::narrow-phonetic-inventory string?)
(s/def ::narrow-phonetic-validation read-validation-value?)
(s/def ::broad-phonetic-inventory string?)
(s/def ::broad-phonetic-validation read-validation-value?)
(s/def ::morpheme-break-is-orthographic boolean?)
(s/def ::morpheme-break-validation read-validation-value?)
(s/def ::phonemic-inventory string?)
(s/def ::morpheme-delimiters (partial common/string-max-len? 255))
(s/def ::punctuation string?)
(s/def ::grammaticalities (partial common/string-max-len? 255))
(s/def ::datetime-modified ::common/datetime-string)
(s/def ::storage-orthography ::orthography/mini-orthography)
(s/def ::input-orthography ::orthography/mini-orthography)
(s/def ::output-orthography ::orthography/mini-orthography)
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

(s/def :write/orthographic-validation write-validation-value?)
(s/def :write/narrow-phonetic-validation write-validation-value?)
(s/def :write/broad-phonetic-validation write-validation-value?)
(s/def :write/morpheme-break-validation write-validation-value?)
(s/def :write/morpheme-break-is-orthographic #{"true" "false"}) ;; weird, but true
(s/def :write/storage-orthography ::orthography/mini-orthography)
(s/def :write/input-orthography ::orthography/mini-orthography)
(s/def :write/output-orthography ::orthography/mini-orthography)
(s/def :write/unrestricted-users (s/coll-of ::common/id :distinct true))

(s/def ::write-application-settings (s/keys :req-un [::object-language-name
                                                     ::object-language-id
                                                     ::metalanguage-name
                                                     ::metalanguage-id
                                                     ::metalanguage-inventory
                                                     :write/orthographic-validation
                                                     ::narrow-phonetic-inventory
                                                     :write/narrow-phonetic-validation
                                                     ::broad-phonetic-inventory
                                                     :write/broad-phonetic-validation
                                                     :write/morpheme-break-is-orthographic
                                                     :write/morpheme-break-validation
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
(s/def ::users ::user/mini-users)
(s/def ::orthographies ::orthography/mini-orthographies)
(s/def ::new-data (s/keys :req-un [::language/languages
                                   ::orthographies
                                   ::users]))

;; NOTE: all seeming app settings updates are actually creates, ie POST requests.
