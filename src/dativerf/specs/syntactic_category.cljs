(ns dativerf.specs.syntactic-category
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::name (partial common/string-max-len? 255))
(s/def ::mini-syntactic-category (s/keys :req-un [::id
                                                  ::name]))
(s/def ::mini-syntactic-categories (s/coll-of ::mini-syntactic-category))
