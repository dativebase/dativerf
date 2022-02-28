(ns dativerf.utils
  (:require [camel-snake-kebab.core :as csk]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(defn commatize [number]
  (pprint/cl-format nil "~,,',:D" number))

(defn kebab->space [s] (str/replace s #"-" " "))

(defn ->kebab-case-recursive [d]
  (walk/postwalk
   (fn [x] (if (keyword? x)
           (if-let [ns (namespace x)]
             (keyword (csk/->kebab-case ns)
                      (csk/->kebab-case (name x)))
             (csk/->kebab-case x))
           x))
   d))
