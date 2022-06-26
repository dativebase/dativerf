(ns dativerf.views.profile
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.routes :as routes]
            [dativerf.subs :as subs]
            [dativerf.views.widgets :as widgets]))

(defn user-title []
      [re-com/title
       :src (at)
       :label "Profile"
       :level :level2])

(def user-rows
  [{:label "Name"
    :key :name
    :getter (fn [{:keys [first-name last-name]}] (str first-name " " last-name))
    :type :string}
   {:label "Role"
    :key :role
    :getter :role
    :type :string}
   {:label "Username"
    :key :username
    :getter :username
    :type :string}
   {:label "Email"
    :key :email
    :getter :email
    :type :string}])

(defn user-info []
  (let [user @(re-frame/subscribe [::subs/user])]
    [re-com/v-box
     :src (at)
     :children
     (for [{:as row :keys [key label getter]} user-rows]
       ^{:key key} [widgets/key-value-row
                    label
                    (assoc row :value (getter user))])]))

(defn profile-tab []
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children [[user-title]
              [user-info]]])

(defmethod routes/tabs :profile [_] [profile-tab])
