(ns dativerf.views.login
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [dativerf.styles :as styles]
   [dativerf.events :as events]
   [dativerf.routes :as routes]
   [dativerf.subs :as subs]))

(defn login-title []
  [re-com/title
   :src   (at)
   :label "Login"
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink
   :src      (at)
   :label    "Home"
   :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn- key-up-login-input [e]
  (when (= "Enter" (.-key e))
    (re-frame/dispatch [::events/user-pressed-enter-in-login])))

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

(defn username-input []
  (let [status-meta @(re-frame/subscribe [:login/username-status])
        status (first status-meta)
        status-icon? (boolean status)
        status-tooltip (or (second status-meta) "")]
    [re-com/box
     :child
     [re-com/input-text
      :attr {:on-key-up key-up-login-input
             :auto-focus true}
      :status status
      :status-icon? status-icon?
      :status-tooltip status-tooltip
      :change-on-blur? false
      :placeholder "username"
      :model @(re-frame/subscribe [:login/username])
      :disabled? @(re-frame/subscribe [:login/inputs-disabled?])
      :on-change (fn [username] (re-frame/dispatch
                                 [::events/user-changed-username
                                  username]))]]))

(defn password-input []
  (let [status-meta @(re-frame/subscribe [:login/password-status])
        status (first status-meta)
        status-icon? (boolean status)
        status-tooltip (or (second status-meta) "")]
    [re-com/box
     :child
     [re-com/input-password
      :attr {:on-key-up key-up-login-input}
      :status status
      :status-icon? status-icon?
      :status-tooltip status-tooltip
      :placeholder "password"
      :change-on-blur? false
      :model @(re-frame/subscribe [:login/password])
      :disabled? @(re-frame/subscribe [:login/inputs-disabled?])
      :on-change (fn [password] (re-frame/dispatch
                                 [::events/user-changed-password
                                  password]))]]))

(defn login-button []
  [re-com/box
   :child
   [re-com/button
    :label "Login"
    :disabled? @(re-frame/subscribe [:login/login-button-disabled?])
    :on-click (fn [_e] (re-frame/dispatch [::events/user-clicked-login]))]])

(defn logout-button []
  [re-com/box
   :child
   [re-com/button
    :label "Logout"
    :disabled? @(re-frame/subscribe [:login/logout-button-disabled?])
    :on-click (fn [_e] (re-frame/dispatch [::events/user-clicked-logout]))]])

(defn user-profile []
      [re-com/hyperlink
       :src (at)
       :label @(re-frame/subscribe [:login/user-name])
       :disabled? @(re-frame/subscribe [:login/user-name-visible?])
       :on-click #(re-frame/dispatch [::events/navigate :profile])])

(defn login-panel []
      [re-com/v-box
       :src (at)
       :gap " 1em "
       :padding " 1em "
       :children [[login-title]
                  [link-to-home-page]
                  [user-profile]
                  [old-instance-select]
                  [username-input]
                  [password-input]
                  [re-com/h-box
                   :children [[login-button]
                              [logout-button]]
                   :gap " 5px "]]])

(defmethod routes/panels :login-panel [] [login-panel])
