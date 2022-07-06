(ns dativerf.specs.user
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

(s/def ::id ::common/id)
(s/def ::first-name (partial common/string-max-len? 255))
(s/def ::last-name (partial common/string-max-len? 255))
(s/def ::role #{"administrator" "contributor" "viewer"})
(s/def ::mini-user (s/keys :req-un [::id
                                    ::first-name
                                    ::last-name
                                    ::role]))
(s/def ::mini-users (s/coll-of ::mini-user))
