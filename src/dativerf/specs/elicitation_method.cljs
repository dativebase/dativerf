(ns dativerf.specs.elicitation-method
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::name (partial common/string-max-len? 255))
(s/def ::mini-elicitation-method (s/keys :req-un [::id
                                                  ::name]))
(s/def ::mini-elicitation-methods (s/coll-of ::mini-elicitation-method))
