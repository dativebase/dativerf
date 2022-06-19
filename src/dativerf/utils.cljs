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

(def handler->tab
  {:forms-last-page :forms
   :form-page :forms
   :forms-page :forms})

(def tab->handler
  {:forms :forms-last-page})

(defn forms-route? [{:keys [handler]}]
  (= :forms (handler handler->tab)))

(defn forms-browse-route? [{:keys [handler]}]
  (some #{handler} [:forms-last-page :forms-page]))
