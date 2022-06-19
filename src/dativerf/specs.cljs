(ns dativerf.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::non-empty-string (s/and string? (complement str/blank?)))

(s/def :old/name ::non-empty-string)
(s/def :old/url ::non-empty-string)
(s/def ::old (s/keys :req-un [:old/name
                              :old/url]))
(s/def ::olds (s/coll-of ::old))
