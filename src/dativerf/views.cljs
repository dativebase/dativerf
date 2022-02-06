(ns dativerf.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [dativerf.styles :as styles]
   [dativerf.events :as events]
   [dativerf.routes :as routes]
   [dativerf.subs :as subs]
   dativerf.views.home
   dativerf.views.login
   dativerf.views.profile))

;; about

(defn about-title []
  [re-com/title
   :src   (at)
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink
   :src      (at)
   :label    "go to Home Page"
   :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn about-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :children [[about-title]
              [link-to-home-page]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
