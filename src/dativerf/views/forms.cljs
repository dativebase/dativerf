(ns dativerf.views.forms
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.form :as form]))

;; Buttons

(defn first-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-fast-rewind"
     :size :smaller
     :disabled? (= 1 current-page)
     :tooltip "first page"
     :on-click (fn [_] (re-frame/dispatch
                        [::events/user-clicked-forms-first-page]))]))

(defn previous-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-skip-previous"
     :size :smaller
     :disabled? (= 1 current-page)
     :tooltip "previous page"
     :on-click (fn [_] (re-frame/dispatch
                        [::events/user-clicked-forms-previous-page]))]))

(defn next-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        last-page @(re-frame/subscribe [::subs/forms-last-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-skip-next"
     :size :smaller
     :disabled? (= current-page last-page)
     :tooltip "next page"
     :on-click (fn [_] (re-frame/dispatch
                        [::events/user-clicked-forms-next-page]))]))

(defn last-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        last-page @(re-frame/subscribe [::subs/forms-last-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-fast-forward"
     :size :smaller
     :disabled? (= current-page last-page)
     :tooltip "last page"
     :on-click (fn [_] (re-frame/dispatch
                        [::events/user-clicked-forms-last-page]))]))

(defn new-form-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-plus"
   :size :smaller
   :tooltip "new form"
   :disabled? true])

(defn expand-all-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-chevron-down"
   :size :smaller
   :tooltip "expand all"
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-expand-all-forms-button]))])

(defn collapse-all-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-chevron-up"
   :size :smaller
   :tooltip "collapse all"
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-collapse-all-forms-button]))])

(defn search-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-search"
   :size :smaller
   :tooltip "search"
   :disabled? true])

(defn export-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-download"
   :size :smaller
   :tooltip "export"
   :disabled? true])

(defn import-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-upload"
   :size :smaller
   :tooltip "import"
   :disabled? true])

(defn labels-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-label"
   :size :smaller
   :tooltip
   (if @(re-frame/subscribe [::subs/forms-labels-on?])
     "hide labels"
     "show labels")
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-forms-labels-button]))])

(defn help-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-help"
   :size :smaller
   :tooltip "help"
   :disabled? true])

(defn items-per-page-select []
  [re-com/single-dropdown
   :src (at)
   :width "80px"
   :choices [{:id 1 :label "1"}
             {:id 2 :label "2"}
             {:id 3 :label "3"}
             {:id 5 :label "5"}
             {:id 10 :label "10"}
             {:id 25 :label "25"}
             {:id 50 :label "50"}
             {:id 100 :label "100"}]
   :model @(re-frame/subscribe [::subs/forms-items-per-page])
   :on-change (fn [items-per-page]
                (re-frame/dispatch
                 [::events/user-changed-items-per-page items-per-page]))])

(defn page-button [page-coordinate]
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        last-page @(re-frame/subscribe [::subs/forms-last-page])
        page-number (+ current-page page-coordinate)
        disabled? (zero? page-coordinate)
        rendered? (> (inc last-page) page-number 0)]
    (when rendered?
      [re-com/button
       :label (utils/commatize page-number)
       :disabled? disabled?
       :tooltip (str "go to page " (utils/commatize page-number))
       :on-click (fn [_] (re-frame/dispatch
                          [::events/user-clicked-go-to-page page-number]))])))

(defn browsing-text []
  [re-com/v-box
   :justify :between
   :max-width "400px"
   :children
   [[re-com/label
     :label
     (str "Forms "
          (utils/commatize @(re-frame/subscribe [::subs/forms-first-form]))
          " to "
          (utils/commatize @(re-frame/subscribe [::subs/forms-last-form]))
          " of "
          (utils/commatize @(re-frame/subscribe [::subs/forms-count]))
          ".")]
    [re-com/label
     :label
     (str "Page "
          (utils/commatize @(re-frame/subscribe [::subs/forms-current-page]))
          " of "
          (utils/commatize @(re-frame/subscribe [::subs/forms-last-page]))
          ".")]]])

;; End Buttons

(defn browse-navigation-top-left []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :children [[new-form-button]
              [expand-all-button]
              [collapse-all-button]]])

(defn browse-navigation-top-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children [[browsing-text]]])

(defn browse-navigation-top-right []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :end
   :children [[search-button]
              [export-button]
              [import-button]
              [labels-button]
              [help-button]]])

(defn browse-navigation-top []
  [re-com/h-box
   :gap "5px"
   :children
   [[browse-navigation-top-left]
    [browse-navigation-top-center]
    [browse-navigation-top-right]]])

(defn browse-navigation-bottom-left []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :children [[first-page-button]
              [previous-page-button]]])

(defn browse-navigation-bottom-center []
  [re-com/h-box
   :size "auto"
   :gap "5px"
   :justify :center
   :children [[items-per-page-select]
              [page-button -3]
              [page-button -2]
              [page-button -1]
              [page-button 0]
              [page-button 1]
              [page-button 2]
              [page-button 3]]])

(defn browse-navigation-bottom-right []
  [re-com/h-box
   :gap "5px"
   :size "auto"
   :justify :end
   :children [[next-page-button]
              [last-page-button]]])

(defn browse-navigation-bottom []
  [re-com/h-box
   :gap "5px"
   :align :center
   :justify :between
   :children
   [[browse-navigation-bottom-left]
    [browse-navigation-bottom-center]
    [browse-navigation-bottom-right]]])

(defn browse-navigation []
  [re-com/v-box
   :gap "5px"
   :children
   [[browse-navigation-top]
    [browse-navigation-bottom]]])

(defn form-index [index]
  [re-com/box
   :width "80px"
   :class (styles/objlang)
   :child [re-com/label :label (str "(" (utils/commatize index) ")")]])

(defn enumerated-form [index form-id]
  [re-com/h-box
   :src (at)
   :padding "1em"
   :children
   [[form-index index]
    [form/igt-form form-id]]])

(defn forms-enumeration []
  (let [first-form @(re-frame/subscribe [::subs/forms-first-form])
        last-form @(re-frame/subscribe [::subs/forms-last-form])
        form-ids @(re-frame/subscribe [::subs/forms-current-page-forms])]
    [re-com/v-box
     :src (at)
     :gap "5px"
     :children
     (for [[index form-id] (map vector
                                (range first-form (inc last-form))
                                form-ids)]
       ^{:key form-id} [enumerated-form index form-id])]))

(defn- forms-tab []
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[browse-navigation]
    [forms-enumeration]]])

(defmethod routes/tabs :forms [] [forms-tab])
