(ns dativerf.specs.tag
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::name ::common/non-blank-string-lte-255)
(s/def ::description string?)
(s/def ::datetime-modified ::common/datetime-string)
(s/def ::tag (s/keys :req-un [::id
                              ::name
                              ::description
                              ::datetime-modified]))
(s/def ::tags (s/coll-of ::tag))

(s/def ::mini-tag (s/keys :req-un [::id
                                   ::name]))
(s/def ::mini-tags (s/coll-of ::mini-tag))
