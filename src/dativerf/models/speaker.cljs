(ns dativerf.models.speaker
  (:require [clojure.string :as str]))

(defn initials [speaker]
  (str (-> speaker :first-name first str/upper-case)
       (-> speaker :last-name first str/upper-case)))
