(ns dativerf.views.widgets
  (:require
   [clojure.string :as str]
   [dativerf.models.utils :as mutils]
   [dativerf.styles :as styles]
   [reagent.ratom :as r]
   [re-com.box :refer [flex-child-style align-style]]
   [re-com.core :as re-com :refer [at]]
   [re-frame.core :as re-frame]))

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

;; This is copy/modified from the re-com source for input-text. See
;; https://github.com/day8/re-com/blob/master/src/re_com/input_text.cljs#L162-L201
(defn field-invalid-warning [message]
  (let [showing? (r/atom false)]
    [re-com/popover-tooltip
     :label message
     :position :right-center
     :status :error
     :showing? showing?
     :anchor
     [:i {:class "zmdi zmdi-hc-fw zmdi-alert-circle zmdi-spinner form-control-feedback"
          :style {:position "static"
                  :height "auto"
                  :color "#d50000"
                  :opacity "1"}
          :on-mouse-over (re-com/handler-fn (reset! showing? true))
          :on-mouse-out  (re-com/handler-fn (reset! showing? false))}]
     :style (merge (flex-child-style "none")
                   (align-style :align-self :center)
                   {:font-size   "130%"
                    :margin-left "4px"})]))

(defn label-with-tooltip
  "Return a label element with a re-com tooltip. The value of the attr param
  must be a keyword that can be used to determine the label text and the tooltip
  text. The metadata map should have a key matching attr. If it does not have
  that key, a human-readable representation of the attr will be used as label
  and tooltip."
  ([metadata attr] (label-with-tooltip metadata styles/wider-attr-label attr))
  ([metadata style attr]
   (let [showing? (r/atom false)
         tooltip (if attr (mutils/description metadata attr) "")]
     [re-com/popover-tooltip
      :label tooltip
      :showing? showing?
      :width (when (and attr (> (count tooltip) 80)) "400px")
      :anchor
      [re-com/box
       :class (str (style) " " (styles/objlang) " " (styles/actionable))
       :align :end
       :padding "0 1em 0 0"
       :attr {:on-mouse-over (re-com/handler-fn (reset! showing? true))
              :on-mouse-out (re-com/handler-fn (reset! showing? false))}
       :child (if attr (mutils/label-str metadata attr) "")]])))

(defn labeled-el
  ([metadata attr el] (labeled-el metadata styles/wider-attr-label attr el))
  ([metadata style attr el]
   [re-com/h-box
    :gap "10px"
    :children
    [[label-with-tooltip metadata style attr]
     el]]))

(defn v-box [els]
  [re-com/v-box
   :src (at)
   :children els])

(defn v-box-gap-with-nils [els]
  [re-com/v-box
   :src (at)
   :class (styles/v-box-gap-with-nils)
   :children els])

(defn- key-up-input [submit-event e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch [submit-event])))

(defn text-input
  "Create a text input widget that syncs with model. The model value is a
   namespaced keyword, a subscription to which will return the value that is
   displayed in the input. The field value is a keyword that can be used to get
   the label and tooltip text from the metadata. The event value is the event
   that should be fired when the value in the input changes. The
   validation-subscription is a keyword for subscribing to a field-specific
   subscription that returns an invalid message if the entered value is invalid."
  ([metadata model field event validation-subscription]
   (text-input metadata model field event validation-subscription {}))
  ([metadata model field event validation-subscription
    {:keys [width submit-event] :or {width "460px"}}]
   (let [invalid-msg @(re-frame/subscribe [validation-subscription field])]
     [labeled-el
      metadata
      field
      [re-com/input-text
       :change-on-blur? false
       :placeholder (mutils/placeholder metadata field)
       :width width
       :model @(re-frame/subscribe [model])
       :status (and invalid-msg :error)
       :status-icon? invalid-msg
       :status-tooltip invalid-msg
       :attr (when submit-event {:on-key-up (partial key-up-input submit-event)})
       :on-change (fn [val] (re-frame/dispatch-sync [event val]))]])))

;; Header

(defn header-left [children]
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :children children])

(defn header-center [children]
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :center
   :children children])

(defn header-right [children]
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :end
   :children children])

(defn header
  "Create a header structure. The keys left, center and right keys are
  collections of child elements that should be in the in the left, center, and
  right sections of the header, respectively."
  [{:keys [left center right]}]
  [re-com/h-box
   :gap "5px"
   :children
   [[header-left left]
    [header-center center]
    [header-right right]]])

(defn footer-center [children]
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children children])

(defn footer [{:keys [center]}]
  [re-com/h-box
   :gap "5px"
   :children [[footer-center center]]])
