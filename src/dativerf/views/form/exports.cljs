(ns dativerf.views.form.exports
  "Define export functions for single forms here. We may eventually want to
  create sub-namespaces such as dativerf.views.form.exports.latex etc.
  To create a new export, add a new map to the exports vec with :id, :label and
  :efn keys. The value of :efn should be an export function. It takes a form and
  returns a string."
  (:require [dativerf.utils :as utils]
            [clojure.string :as str]))

;; Plain text export
;; NOTE: we probably want a better plain-text export than this. This is just an
;; initial example.

(def plain-text-export-fields
  [{:label "transcription"
    :getter (fn [{:keys [transcription grammaticality]}]
              (str grammaticality transcription))}
   {:label "morpheme break"
    :getter :morpheme-break}
   {:label "morpheme gloss"
    :getter :morpheme-gloss}
   {:label "translations"
    :getter (fn [{:keys [translations]}]
              (->> translations
                   (map (fn [{:keys [transcription grammaticality]}]
                          (str \' grammaticality transcription \')))
                   (str/join ", ")))}])

(defn plain-text-export [form]
  (->> plain-text-export-fields
       (map (fn [{:keys [label getter]}]
              (str label ": " (getter form))))
       (str/join "\n")))

;; JSON export

(defn prepare-form-for-jsonification [form]
  (-> form
      (dissoc :dative/fetched-at)
      (update :uuid str)
      utils/->snake-case-recursive))

(defn json-export [form]
  (-> form prepare-form-for-jsonification utils/->pretty-json))

;; Leipzig IGT export
;;
;; The canonical reference for this XML version of the Leipzig IGT format seems
;; to be the JavaScript implementation whose source is available at
;; https://github.com/bdchauvette/leipzig.js/. The canonical linguistic
;; reference for Leipzig IGT appears to be
;; https://www.eva.mpg.de/lingua/resources/glossing-rules.php.
;; Example::
;;
;;   <div data-gloss>
;;     <p>n parqu a hanze</p>
;;     <p>1PS.SUBJ park FV outside</p>
;;     <p>'I park outside.'</p>
;;   </div>

(defn p-el [igt-line] (str "  <p>" igt-line "</p>"))

(defn enclose-in-quotes [string] (str \' string \'))

(defn p-els-for-non-nil-val
  ([key form] (p-els-for-non-nil-val key identity form))
  ([key formatter form]
   [(some-> form key str/trim not-empty formatter p-el)]))

(defn p-els-for-grammaticalized-transcription
  ([form] (p-els-for-grammaticalized-transcription identity form))
  ([formatter {:as tmp :keys [transcription grammaticality]}]
   (p-els-for-non-nil-val :v formatter {:v (str grammaticality transcription)})))

(defn p-els-for-translations [{:keys [translations]}]
  (mapcat (partial p-els-for-grammaticalized-transcription enclose-in-quotes)
          translations))

(def leipzig-igt-rules
  [(partial p-els-for-non-nil-val :narrow-phonetic-transcription)
   (partial p-els-for-non-nil-val :phonetic-transcription)
   p-els-for-grammaticalized-transcription
   (partial p-els-for-non-nil-val :morpheme-break)
   (partial p-els-for-non-nil-val :morpheme-gloss)
   p-els-for-translations])

(defn leipzig-igt-export [form]
  (str "<div data-gloss>\n"
       (or (some->> leipzig-igt-rules
                    (mapcat (fn [rule] (rule form)))
                    (filter some?)
                    not-empty
                    (str/join "\n"))
           "  <p>no data</p>")
       "\n</div>"))

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
