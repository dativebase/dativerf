(ns dativerf.views.forms.search
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require [clojure.string :as str]
            [dativerf.events :as events]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com]))

(defn- key-up-input [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch
     [::events/user-clicked-search-forms-button])))

(def model-metadata
  {:search-forms/search-input
   {:label "search"
    :tooltip "Search all forms."}})

(defn model-x [x model]
  (or (-> model model-metadata x)
      (-> model name utils/kebab->space)))
(def model-placeholder (partial model-x :placeholder))
(def model-tooltip (partial model-x :tooltip))

(defn search []
   [re-com/h-box
    :gap "10px"
    :children
    [[re-com/input-text
      :change-on-blur? false
      :placeholder "input"
      :width "500px"
      :model @(re-frame/subscribe [:search-forms/search-input])
      :attr {:auto-focus true
             :on-key-up key-up-input}
      :on-change (fn [input] (re-frame/dispatch-sync
                               [::events/user-changed-search-input
                                input]))]]])

(defn text-input [model event]
    [re-com/h-box
       :gap "10px"
       :justify :center
       :children
       [[re-com/input-text
         :change-on-blur? false
         :placeholder (model-placeholder model)
         :width "560px"
         :model @(re-frame/subscribe [model])
         :attr {:on-key-up key-up-input}
         :on-change (fn [val] (re-frame/dispatch-sync [event val]))]]])

(defn header-left []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :children
   [[re-com/md-circle-icon-button
     :md-icon-name "zmdi-close"
     :size :smaller
     :tooltip "hide search interface"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-new-search-forms-button]))]]])

(defn header-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children [[re-com/box :child "Search over Forms"]]])

(defn header-right []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :end
   :children
   [[re-com/md-circle-icon-button
     :md-icon-name "zmdi-refresh-sync"
     :size :smaller
     :tooltip "clear this search: reset search input field to its default value"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-clear-search-forms-interface]))]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-help"
     :size :smaller
     :tooltip "help with searching forms"
     :disabled? true
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/user-clicked-help-search-forms]))]]])

(defn header []
  [re-com/h-box
   :gap "5px"
   :children
   [[header-left]
    [header-center]
    [header-right]]])

(defn inputs []
  [re-com/v-box
   :class (str (styles/v-box-gap-with-nils) " " (styles/objlang))
   :children
   [[text-input :search-forms/search-input
     ::events/user-changed-search-input]]])

(defn search-button []
  [re-com/box
   :child
   [re-com/button
    :label "Search"
    :tooltip "search forms"
    :on-click (fn [_e] (re-frame/dispatch
                        [::events/user-clicked-search-forms-button]))]])

(defn footer-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children
   [[search-button]]])

(defn footer []
  [re-com/h-box
   :gap "5px"
   :children
   [[footer-center]]])

(defn interface []
  (when @(re-frame/subscribe [::subs/forms-search-interface-visible?])
        [re-com/v-box
       :gap "10px"
       :class (styles/form-sub-interface)
       :children
       [[header]
        [inputs]
        [footer]]]))