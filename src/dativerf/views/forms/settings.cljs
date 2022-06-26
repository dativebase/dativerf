(ns dativerf.views.forms.settings
  (:require [dativerf.db :as db]
            [dativerf.events :as events]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com]))

(defn header-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children [[re-com/box :child "Form Settings"]]])

(defn header []
  [re-com/h-box
   :gap "5px"
   :children
   [[header-center]]])

(defn make-all-fields-visible-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-check-all"
   :size :smaller
   :tooltip "make all fields visible"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-make-all-form-fields-visible-button]))])

(defn make-no-fields-visible-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-square-o"
   :size :smaller
   :tooltip "hide all fields that can be hidden"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-make-no-form-fields-visible-button]))])

(defn restore-default-field-visibity-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-format-clear-all"
   :size :smaller
   :tooltip "restore default field visibility"
   :on-click
   (fn [_]
     (re-frame/dispatch
      [::events/user-clicked-restore-default-field-visibility-button]))])

(defn field-visibility-controls []
  (when @(re-frame/subscribe
          [::subs/forms-settings-field-visibility-interface-visible?])
    [re-com/v-box
     :gap "10px"
     :children
     [[re-com/h-box
       :gap "10px"
       :children [[make-all-fields-visible-button]
                  [make-no-fields-visible-button]
                  [restore-default-field-visibity-button]]]
      [re-com/box
       :child
       [re-com/selection-list
        :height "200px"
        :choices
        (for [k (->> db/default-form-state
                     keys
                     (filter (fn [k] (not (some #{k}
                                                db/always-visible-form-fields))))
                     sort)]
          {:id k :label (-> k name utils/kebab->space)})
        :model @(re-frame/subscribe [::subs/visible-form-fields])
        :on-change (fn [visible-form-fields]
                     (re-frame/dispatch
                      [::events/user-selected-visible-form-fields
                       visible-form-fields]))]]]]))

(defn field-visibility []
  (let [interface-visible?
        @(re-frame/subscribe
          [::subs/forms-settings-field-visibility-interface-visible?])]
    [re-com/v-box
     :gap "10px"
     :children
     [[:h4 "Field Visibility"]
      [re-com/h-box
       :gap "10px"
       :children
       [[re-com/md-circle-icon-button
         :md-icon-name (if interface-visible?
                         "zmdi-chevron-down"
                         "zmdi-chevron-right")
         :size :smaller
         :tooltip (if interface-visible?
                    "hide field visibility details"
                    "show field visibility details")
         :on-click
         (fn [_]
           (re-frame/dispatch
            [::events/user-clicked-toggle-form-field-visibility-interface]))]
        [re-com/box :child "Control the visibility of form fields."]]]
      [field-visibility-controls]]]))

(defn interface []
  (when @(re-frame/subscribe [::subs/forms-settings-interface-visible?])
    [re-com/v-box
     :gap "10px"
     :class (styles/form-sub-interface)
     :children
     [[header]
      [field-visibility]]]))
