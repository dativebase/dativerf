(ns dativerf.models.utils
  (:require [dativerf.utils :as utils]))

(defn- get-x-from-metadata [x metadata attr]
  (or (-> attr metadata x)
      (-> attr name utils/kebab->space)))
(def label-str (partial get-x-from-metadata :label))
(def placeholder (partial get-x-from-metadata :placeholder))
(def description (partial get-x-from-metadata :description))
