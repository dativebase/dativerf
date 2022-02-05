(ns dativerf.utils
  (:require [clojure.walk :as walk]
            [camel-snake-kebab.core :as csk]))

(defn ->kebab-case-recursive [d]
  (walk/postwalk
   (fn [x] (if (keyword? x)
           (if-let [ns (namespace x)]
             (keyword (csk/->kebab-case ns)
                      (csk/->kebab-case (name x)))
             (csk/->kebab-case x))
           x))
   d))
