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
       :src   (at)
       :label "Hi ..."
       :level :level1])

(defn profile-panel []
      [re-com/v-box
       :src (at)
       :gap "1em"
       :padding "1em"
       :children [user-title]])

(defmethod routes/panels :profile-panel [] [profile-panel])
