(ns dativerf.specs.common
  (:require [cljs-time.core :as time]
            [cljs-time.coerce :as timec]
            [cljs-time.format :as timef]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [dativerf.utils :as utils]))

;; Generators

(def datetime-generator
  "Generates a DateTime instance between 1800 and 2030."
  (let [day-min (timec/to-long (time/date-time 1800))
        day-max (timec/to-long (time/date-time 2030))]
    (gen/fmap (fn [uts] (timec/from-long uts))
              (gen/choose day-min day-max))))

(def date-string-generator
  (gen/fmap (fn [dt] (timef/unparse (timef/formatters :date) dt))
            datetime-generator))

(def datetime-string-generator
  (gen/fmap (fn [dt] (timef/unparse
                      (timef/formatters :date-hour-minute-second-ms) dt))
            datetime-generator))

(def uuid-string-generator (gen/fmap str (s/gen uuid?)))

;; Predicates

(defn lte-1 [s] (<= (count s) 1))
(defn lte-2 [s] (<= (count s) 2))
(defn lte-3 [s] (<= (count s) 3))
(defn lte-20 [s] (<= (count s) 20))
(defn lte-25 [s] (<= (count s) 25))
(defn lte-100 [s] (<= (count s) 100))
(defn lte-150 [s] (<= (count s) 150))
(defn lte-255 [s] (<= (count s) 255))
(defn lte-510 [s] (<= (count s) 510))
(defn lte-1000 [s] (<= (count s) 1000))
(defn lte-1023 [s] (<= (count s) 1023))

(defn uuid-string? [x]
  (and (string? x)
       (re-find #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
                x)))

;; Specs

(s/def ::id pos-int?)

(s/def ::date-string
  (s/with-gen utils/date-string? (constantly date-string-generator)))

(s/def ::datetime-string
  (s/with-gen utils/datetime-string? (constantly datetime-string-generator)))

(s/def ::non-blank-string (s/and string? (complement str/blank?)))

(s/def ::non-blank-string-lte-255 (s/and ::non-blank-string lte-255))
(s/def ::non-blank-string-lte-510 (s/and ::non-blank-string lte-510))

(s/def ::string-lte-1 (s/and string? lte-1))
(s/def ::string-lte-2 (s/and string? lte-2))
(s/def ::string-lte-3 (s/and string? lte-3))
(s/def ::string-lte-20 (s/and string? lte-20))
(s/def ::string-lte-25 (s/and string? lte-25))
(s/def ::string-lte-100 (s/and string? lte-100))
(s/def ::string-lte-150 (s/and string? lte-150))
(s/def ::string-lte-255 (s/and string? lte-255))
(s/def ::string-lte-510 (s/and string? lte-510))
(s/def ::string-lte-1023 (s/and string? lte-1023))
(s/def ::string-lte-1000 (s/and string? lte-1000))

(s/def ::uuid-string
  (s/with-gen uuid-string? (constantly uuid-string-generator)))
