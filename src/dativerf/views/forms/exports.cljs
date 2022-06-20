(ns dativerf.views.forms.exports
  "Define export functions for multiple forms here. To create a new export, add
  a new map to the exports vec with :id, :label and :efn keys. The value of :efn
  should be an export function. It takes a collection of forms and returns a
  string."
  (:require [clojure.string :as str]
            [dativerf.views.form.exports :as form-exports]
            [dativerf.utils :as utils]))

(defn plain-text-export [forms]
  (->> forms
       (map form-exports/plain-text-export)
       (str/join "\n\n")))

;; JSON export

(defn json-export [forms]
  (->> forms
       (map form-exports/prepare-form-for-jsonification)
       utils/->pretty-json))

;; API

(def exports
  [{:id :plain-text
    :label "Plain Text"
    :efn plain-text-export}
   {:id :json
    :label "JSON"
    :efn json-export}])

(defn export [export-id]
  (first (for [{:as e :keys [id]} exports
               :when (= export-id id)] e)))
