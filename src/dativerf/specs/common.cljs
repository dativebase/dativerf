(ns dativerf.specs.common
  (:require [clojure.spec.alpha :as s]
            [dativerf.utils :as utils]))

(s/def ::id pos-int?)
(s/def ::date-string utils/date-string?)
(s/def ::datetime-string utils/datetime-string?)

(defn string-max-len? [len string] (and (string? string) (<= (count string) len)))
