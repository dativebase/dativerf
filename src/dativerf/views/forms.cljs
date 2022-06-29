(ns dativerf.views.forms
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.form :as form]
            [dativerf.views.forms.new :as forms-new]
            [dativerf.views.forms.settings :as forms-settings]
            [dativerf.exporters.forms :as forms-exporter]
            [dativerf.views.widgets :as widgets]))

;; Buttons

(defn- forms-page-route []
  {:handler :forms-page
   :route-params
   {:old @(re-frame/subscribe [::subs/old-slug])
    :items-per-page @(re-frame/subscribe
                      [::subs/forms-items-per-page])}})

(defn first-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-fast-rewind"
     :size :smaller
     :disabled? (= 1 current-page)
     :tooltip "first page"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/navigate
         (assoc-in (forms-page-route) [:route-params :page] 1)]))]))

(defn previous-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-skip-previous"
     :size :smaller
     :disabled? (= 1 current-page)
     :tooltip "previous page"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/navigate
         (assoc-in (forms-page-route)
                   [:route-params :page]
                   (dec current-page))]))]))

(defn next-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        last-page @(re-frame/subscribe [::subs/forms-last-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-skip-next"
     :size :smaller
     :disabled? (= current-page last-page)
     :tooltip "next page"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/navigate
         (assoc-in (forms-page-route)
                   [:route-params :page]
                   (inc current-page))]))]))

(defn last-page-button []
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        last-page @(re-frame/subscribe [::subs/forms-last-page])]
    [re-com/md-circle-icon-button
     :md-icon-name "zmdi-fast-forward"
     :size :smaller
     :disabled? (= current-page last-page)
     :tooltip "last page"
     :on-click
     (fn [_]
       (re-frame/dispatch
        [::events/navigate
         (assoc-in (forms-page-route)
                   [:route-params :page]
                   last-page)]))]))

(defn new-form-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-plus"
   :size :smaller
   :tooltip (if @(re-frame/subscribe [::subs/forms-new-form-interface-visible?])
              "hide new form interface"
              "show new form interface")
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-new-form-button]))])

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
   :tooltip (if @(re-frame/subscribe [::subs/forms-export-interface-visible?])
              "hide export interface"
              "show export interface")
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-export-forms-button]))])

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

(defn back-to-browse-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-fast-rewind"
   :size :smaller
   :tooltip "back to forms browse"
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-back-to-browse-button]))])

(defn settings-button []
  [re-com/md-circle-icon-button
   :md-icon-name "zmdi-settings"
   :size :smaller
   :tooltip (if @(re-frame/subscribe [::subs/forms-settings-interface-visible?])
              "hide form settings"
              "show form settings")
   :on-click (fn [_] (re-frame/dispatch
                      [::events/user-clicked-form-settings-button]))])

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
       :tooltip (if disabled?
                  (str "on page " (utils/commatize page-number))
                  (str "go to page " (utils/commatize page-number)))
       :on-click
       (fn [_]
         (re-frame/dispatch
          [::events/navigate
           (assoc-in (forms-page-route)
                     [:route-params :page]
                     page-number)]))])))

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
              [search-button]
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
   :children [[export-button]
              [import-button]
              [labels-button]
              [settings-button]
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

(defn forms-export-select []
  [re-com/single-dropdown
   :src (at)
   :width "250px"
   :choices forms-exporter/exports
   :model @(re-frame/subscribe [::subs/forms-export-format])
   :tooltip "choose an export format"
   :on-change
   (fn [export-id]
     (re-frame/dispatch
      [::events/user-selected-forms-export export-id]))])

(defn forms-export [export-string]
  [re-com/box
   :class (styles/export)
   :child [:pre {:style {:margin-bottom "0px"}} export-string]])

(defn export-forms-interface []
  (when @(re-frame/subscribe [::subs/forms-export-interface-visible?])
    (let [export-fn (:efn (forms-exporter/export @(re-frame/subscribe
                                            [::subs/forms-export-format])))
          forms @(re-frame/subscribe
                  [::subs/forms-by-ids
                   @(re-frame/subscribe [::subs/forms-current-page-forms])])
          export-string (export-fn (vals forms))]
      [re-com/v-box
       :class (styles/export-interface)
       :children
       [[re-com/h-box
         :gap "10px"
         :children [[forms-export-select]
                    [widgets/copy-button export-string "forms export"]]]
        [forms-export export-string]]])))

(defn delete-form-modal []
  (when-let [form-to-delete @(re-frame/subscribe [::subs/form-to-delete])]
    [re-com/modal-panel
     :src (at)
     :backdrop-color "grey"
     :backdrop-opacity 0.4
     :backdrop-on-click (fn [] (re-frame/dispatch
                                [::events/abort-form-deletion]))
     :child
     [re-com/v-box
      :children
      [[re-com/alert-box
        :alert-type :danger
        :heading (str "Delete form " form-to-delete)
        :body (str "Are you sure that you want to delete the form with ID "
                   form-to-delete "?")]
       [re-com/h-box
        :gap "10px"
        :justify :end
        :children
        [[re-com/button
          :label "Cancel"
          :attr {:auto-focus true}
          :tooltip "don't actually delete this form"
          :on-click (fn [] (re-frame/dispatch [::events/abort-form-deletion]))]
         [re-com/button
          :label "Ok"
          :tooltip "go ahead and delete this form"
          :class "btn-danger"
          :on-click (fn [_e] (re-frame/dispatch
                              [::events/delete-form form-to-delete]))]]]]]]))

(defn- forms-tab []
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[browse-navigation]
    [forms-settings/interface]
    [forms-new/interface]
    [export-forms-interface]
    [forms-enumeration]
    [delete-form-modal]]])

(defn- form-navigation []
  [re-com/h-box
   :src (at)
   :gap "5px"
   :size "auto"
   :children [[back-to-browse-button]
              [labels-button]]])

(defn- form-tab [form]
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[form-navigation]
    [form/igt-form (:uuid form)]
    [delete-form-modal]]])

(defmethod routes/tabs :forms-page
  [{{:keys [page items-per-page]} :route-params}]
  (let [current-page @(re-frame/subscribe [::subs/forms-current-page])
        page (js/parseInt page)
        form-ids @(re-frame/subscribe [::subs/forms-current-page-forms])
        forms-count @(re-frame/subscribe [::subs/forms-count])
        forms (filter some?
                      (for [form-id form-ids]
                        @(re-frame/subscribe [::subs/form-by-id form-id])))]
    (if (or (zero? forms-count)
            (and (= page current-page) (= (count form-ids) (count forms))))
      [forms-tab]
      (do (re-frame/dispatch [::events/fetch-forms-page page items-per-page])
          [re-com/throbber :size :large]))))

(defmethod routes/tabs :forms-last-page [_]
  (let [last-page @(re-frame/subscribe [::subs/forms-last-page])
        current-page @(re-frame/subscribe [::subs/forms-current-page])
        form-ids @(re-frame/subscribe [::subs/forms-current-page-forms])
        force-reload? @(re-frame/subscribe [::subs/forms-force-reload?])
        forms (filter some?
                      (for [form-id form-ids]
                        @(re-frame/subscribe [::subs/form-by-id form-id])))]
    (cond force-reload?
          (do (re-frame/dispatch [::events/fetch-forms-last-page])
              (re-frame/dispatch [::events/turn-off-force-forms-reload])
              [re-com/throbber :size :large])
          (and last-page
               (= last-page current-page)
               (= (count form-ids) (count forms)))
          (re-frame/dispatch
           [::events/navigate
            (assoc-in (forms-page-route) [:route-params :page] last-page)])
          :else
          (do (re-frame/dispatch [::events/fetch-forms-last-page])
              [re-com/throbber :size :large]))))

(defmethod routes/tabs :form-page [{{:keys [id]} :route-params}]
  (if-let [form @(re-frame/subscribe [::subs/form-by-int-id (js/parseInt id)])]
    [form-tab form]
    (do
      (re-frame/dispatch [::events/fetch-form id])
      [re-com/throbber :size :large])))
