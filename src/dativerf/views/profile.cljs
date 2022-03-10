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
