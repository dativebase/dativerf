(ns dativerf.views.widgets
  (:require
   [clojure.string :as str]
   [re-com.core :as re-com :refer [at]]))

(def min-height-value-cell "20px")

(defmulti value-cell :type)

(defmethod value-cell :string
  [{:keys [value]}]
  [re-com/p
   {:style {:min-height min-height-value-cell}}
   value])

(defmethod value-cell :character-sequence-as-string
  [{:keys [value]}]
  [re-com/p
   {:style {:min-height min-height-value-cell}}
   (->> value (str/join " "))])

(defmethod value-cell :boolean
  [{:keys [value]}]
  [re-com/p
   {:style {:min-height min-height-value-cell}}
   (if value "true" "false")])

(defmethod value-cell :coll-of-users
  [{users :value}]
  [re-com/p
   {:style {:min-height min-height-value-cell}}
   (str/join ", " (for [{:keys [first-name last-name]} users]
                             (str first-name " " last-name)))])

(defmethod value-cell :coll-of-translations
  [{translations :value}]
  [re-com/v-box
   ;; {:style {:min-height min-height-value-cell}}
   :children
   (for [{:keys [id transcription grammaticality]} translations]
     ^{:key id} [re-com/p (str grammaticality transcription)])])

(defmethod value-cell :default
  [value]
  (println "WARNING: unrepresentable entity: " (str value))
  [re-com/p ""])

(defn key-value-row [key value]
  [re-com/h-box
   :src (at)
   :children
   [[re-com/label
     :label key
     :width "240px"]
    [value-cell value]]])

;; From https://gist.github.com/rotaliator/73daca2dc93c586122a0da57189ece13
(defn- copy-to-clipboard [val]
  (let [el (js/document.createElement "textarea")]
    (set! (.-value el) val)
    (.appendChild js/document.body el)
    (.select el)
    (js/document.execCommand "copy")
    (.removeChild js/document.body el)))

(defn copy-button [string string-description]
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-copy"
   :size :smaller
   :tooltip (str "copy " string-description " to clipboard")
   :on-click (fn [e]
               (.stopPropagation e)
               (copy-to-clipboard string))])
