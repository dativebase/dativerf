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
