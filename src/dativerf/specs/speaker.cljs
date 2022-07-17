(ns dativerf.specs.speaker
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::first-name (partial common/string-max-len? 255))
(s/def ::last-name (partial common/string-max-len? 255))
(s/def ::dialect (partial common/string-max-len? 255))
(s/def ::mini-speaker (s/keys :req-un [::id
                                       ::first-name
                                       ::last-name
                                       ::dialect]))
(s/def ::mini-speakers (s/coll-of ::mini-speaker))
