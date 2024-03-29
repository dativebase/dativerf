(ns dativerf.specs.orthography
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::name ::common/string-lte-255)
(s/def ::orthography string?)
(s/def ::lowercase boolean?)
(s/def ::initial-glottal-stops boolean?)

(s/def ::mini-orthography (s/keys :req-un [::id
                                           ::name
                                           ::orthography
                                           ::lowercase
                                           ::initial-glottal-stops]))
(s/def ::mini-orthographies (s/coll-of ::mini-orthography))
