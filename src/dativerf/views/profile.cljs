(ns dativerf.views.profile
  (:require
    [re-frame.core :as re-frame]
    [re-com.core :as re-com :refer [at]]
    [dativerf.styles :as styles]
    [dativerf.events :as events]
    [dativerf.routes :as routes]
    [dativerf.subs :as subs]))

(defn user-title []
      [re-com/title
       :src (at)
       :label "Profile"
       :level :level2])

(defn old-instance-select []
      [re-com/h-box
       :gap "10px"
       :align :center
       :children
       [[re-com/box
         :child [re-com/single-dropdown
                 :src (at)
                 :width "250px"
                 :choices @(re-frame/subscribe [::subs/olds])
                 :model @(re-frame/subscribe [::subs/old])
                 :label-fn :name
                 :filter-box? true
                 :on-change
                 (fn [x]
                     (re-frame/dispatch
                       [::events/user-changed-current-old-instance x]))
                 :disabled? @(re-frame/subscribe [:login/inputs-disabled?])]]]])


(defn user-info []
      (let [user @(re-frame/subscribe [::subs/user])]
           [:ul
            [:li (str "Name: " (:first-name user) " " (:last-name user))]
            [:li (str "Email: " (:email user))]
            ]
           ))

(defn profile-tab []
      [re-com/v-box
       :src (at)
       :gap "1em"
       :padding "1em"
       :children [
                  [user-title]
                  [user-info]
                  ]])

(defmethod routes/tabs :profile [] [profile-tab])
