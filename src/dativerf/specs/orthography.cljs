(ns dativerf.specs.orthography
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::name (partial common/string-max-len? 255))
(s/def ::orthography string?)
(s/def ::lowercase boolean?)
(s/def ::initial_glottal_stops boolean?)

(s/def ::mini-orthography (s/keys :req-un [::id
                                           ::name
                                           ::orthography
                                           ::lowercase
                                           ::initial_glottal_stops]))
(s/def ::mini-orthographies (s/coll-of ::mini-orthography))
