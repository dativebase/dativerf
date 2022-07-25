(ns dativerf.specs.language
  "This is the ISO-639-3 dataset."
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

;; WARNING: this spec is not currently used. See
;; dativerf.models.application-settings for details of how the languages
;; collection is converted into a leaner map from Id strings to Ref_Name strings.
(s/def ::id ::common/string-lte-3) ;; Id
(s/def ::part-2-b ::common/string-lte-3) ;; Part2B
(s/def ::part-2-t ::common/string-lte-3) ;; Part2T
(s/def ::part-1 ::common/string-lte-2) ;; Part1
(s/def ::scope ::common/string-lte-2) ;; Scope
(s/def ::type ::common/string-lte-1) ;; Type
(s/def ::ref-name ::common/string-lte-150) ;; Ref_Name
(s/def ::comment ::common/string-lte-150) ;; Comment
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
