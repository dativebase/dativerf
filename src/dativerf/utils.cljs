(ns dativerf.utils
  (:require [camel-snake-kebab.core :as csk]
            [cljs.pprint :as pprint]
            [cljs-time.format :as timef]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [goog.string :as gstring]
            [goog.string.format]))

(defn commatize [number]
  (pprint/cl-format nil "~,,',:D" number))

(defn kebab->space [s] (str/replace s #"-" " "))

(defn modify-form-keywords-recursive [modifier d]
  (walk/postwalk
   (fn [x] (if (keyword? x) (modifier x) x))
   d))

(defn ->kebab-case [kw]
  (if-let [ns (namespace kw)]
    (keyword (csk/->kebab-case ns)
             (csk/->kebab-case (name kw)))
    (csk/->kebab-case kw)))

(defn ->snake-case [kw]
  (if-let [ns (namespace kw)]
    (keyword (csk/->snake_case_keyword ns)
             (csk/->snake_case_keyword (name kw)))
    (csk/->snake_case_keyword kw)))

(def ->kebab-case-recursive
  (partial modify-form-keywords-recursive ->kebab-case))

(def ->snake-case-recursive
  (partial modify-form-keywords-recursive ->snake-case))

(defn remove-namespace [kw] (-> kw name keyword))

(def remove-namespaces-recursive
  (partial modify-form-keywords-recursive remove-namespace))

(defn ->pretty-json [x] (.stringify js/JSON (clj->js x) nil 2))

(defn set-kw-ns [ns kw] (->> kw name (keyword ns)))

(defn select-keys-by-ns [ns m]
  (->> m
       (filter (fn [[k _]] (and (keyword? k) (= ns (namespace k)))))
       (into {})))

(def set-kw-ns-to-form (partial set-kw-ns "form"))

(def handler->tab
  {:forms-last-page :forms
   :form-page :forms
   :forms-page :forms
   :old-settings-input-validation :old-settings})

(def tab->handler
  {:forms :forms-last-page})

(defn forms-route? [{:keys [handler]}]
  (= :forms (handler handler->tab handler)))

(defn forms-browse-route? [{:keys [handler]}]
  (some #{handler} [:forms-last-page :forms-page]))

(defn old-settings-route? [{:keys [handler]}]
  (= :old-settings (handler handler->tab handler)))

(defn goog-date-utc-date-time->mm-dd-yyyy-string [dt]
  (str
   (gstring/format "%02d" (inc (.getMonth dt)))
   "/"
   (gstring/format "%02d" (.getDate dt))
   "/"
   (gstring/format "%02d" (.getYear dt))))

;; Format returned is 'Fri, 22 Jan 2016 21:43:54 Z'
;; Probably want something different eventually.
(defn datetime->human-string [datetime]
  (timef/unparse (timef/formatters :rfc822) datetime))

;; WARNING: hacky
;; Format returned is 'Fri, 22 Jan 2016'
;; Probably want something different eventually.
(defn date->human-string [date]
  (when date
    (let [date-str (datetime->human-string date)
          time-sfx (->> date-str reverse (take 11) reverse (apply str))]
      (if (and (= " " (first time-sfx))
               (= "Z" (last time-sfx)))
        (->> date-str reverse (drop 11) reverse (apply str))
        date-str))))

(defn parse-date-string [x]
  (try (timef/parse (timef/formatters :date) x)
       (catch js/Error _)))

(defn date-string? [^string x]
  (and (string? x)
       (parse-date-string x)))

(defn parse-datetime-string
  "Parse a datetime string like 2022-02-23T18:27:49.628337. To do this, we must
  remove the last 3 digits, reducing the microsecond precision to millisecond
  precision."
  [x]
  (try (timef/parse
        (timef/formatters :date-hour-minute-second-ms)
        (str/replace x #"\.(\d{3})\d{3}$" ".$1"))
       (catch js/Error _)))

(defn datetime-string? [^string x]
  (and (string? x)
       (parse-datetime-string x)))

(defn empty-string->nil [s] (some-> s str/trim not-empty))

(defn get-empty-string->nil [m k] (some-> m k empty-string->nil))
