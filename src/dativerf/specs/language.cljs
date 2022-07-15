(ns dativerf.specs.language
  "This is the ISO-639-3 dataset."
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

;; WARNING: this spec is not currently used. See
;; dativerf.models.application-settings for details of how the languages
;; collection is converted into a leaner map from Id strings to Ref_Name strings.
(s/def ::id (partial common/string-max-len? 3)) ;; Id
(s/def ::part-2-b (partial common/string-max-len? 3)) ;; Part2B
(s/def ::part-2-t (partial common/string-max-len? 3)) ;; Part2T
(s/def ::part-1 (partial common/string-max-len? 2)) ;; Part1
(s/def ::scope (partial common/string-max-len? 1)) ;; Scope
(s/def ::type (partial common/string-max-len? 1)) ;; Type
(s/def ::ref-name (partial common/string-max-len? 150)) ;; Ref_Name
(s/def ::comment (partial common/string-max-len? 150)) ;; Comment
(s/def ::datetime-modified ::common/datetime-string) ;; datetime_modified
(s/def ::language (s/keys :req-un [::id
                                   ::part-2-b
                                   ::part-2-t
                                   ::part-1
                                   ::scope
                                   ::type
                                   ::ref-name
                                   ::comment
                                   ::datetime-modified]))
(s/def ::languages (s/coll-of ::language))
