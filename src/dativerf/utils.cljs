(ns dativerf.utils
  (:require [camel-snake-kebab.core :as csk]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [clojure.walk :as walk]))

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
