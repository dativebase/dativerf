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

;; Leipzig IGT export

;; I just enclose the collection of <div data-gloss> elements in a generic <div>
;; for now. It's not clear to me whether we want to do more here.
(defn leipzig-igt-export [forms]
  (str
   "<div>\n\n"
   (->> forms
        (map form-exports/leipzig-igt-export)
        (str/join "\n\n"))
   "\n\n</div>"))

;; API

(def exports
  [{:id :plain-text
    :label "Plain Text"
    :efn plain-text-export}
   {:id :json
    :label "JSON"
    :efn json-export}
   {:id :leipzig-igt
    :label "Leipzig IGT"
    :efn leipzig-igt-export}])

(defn export [export-id]
  (first (for [{:as e :keys [id]} exports
               :when (= export-id id)] e)))
