(ns dativerf.models.old
  (:require [dativerf.specs :as specs]
            [dativerf.utils :as utils]
            [clojure.spec.alpha :as s]))

(defn olds-response->olds [olds-response]
  (let [olds (utils/->kebab-case-recursive olds-response)
        explain-data (s/explain-data ::specs/olds olds)]
    (when explain-data
      (throw (ex-info "OLDs from server are malformed"
                      {:olds olds
                       :explain-data explain-data})))
    olds))
