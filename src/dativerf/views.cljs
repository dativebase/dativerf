(ns dativerf.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            dativerf.views.home
            dativerf.views.login))

(defn title []
  (let [user @(re-frame/subscribe [::subs/user])
        old-id @(re-frame/subscribe [::subs/old])
        olds @(re-frame/subscribe [::subs/olds])
        old-name (some->> olds
                          (filter (fn [o] (= old-id (:url o))))
                          first
                          :name)
        title (if (and user old-name)
                (str "Dative: " old-name)
                "Dative")]
    [re-com/title
     :src   (at)
     :label title
     :level :level1
     :class (styles/level1)]))

(def menu-tabs
  [{:id :home :label "Home" :authenticated? nil}
   {:id :forms :label "Forms" :authenticated? true}
   {:id :files :label "Files" :authenticated? true}
   {:id :collections :label "Collections" :authenticated? true}
   {:id :login :label "Login" :authenticated? false}
   {:id :logout :label "Logout" :authenticated? true}])

(defn- unauthenticated-tabs [tabs]
  (filter (fn [{:keys [authenticated?]}]
            (or (false? authenticated?)
                (nil? authenticated?)))
          tabs))

(defn- authenticated-tabs [tabs]
  (filter (fn [{:keys [authenticated?]}]
            (or (true? authenticated?)
                (nil? authenticated?)))
          tabs))

(defn menu []
  (let [user @(re-frame/subscribe [::subs/user])
        model @(re-frame/subscribe [::subs/active-tab])
        authenticated? (boolean user)
        tabs (if authenticated?
               (authenticated-tabs menu-tabs)
               (unauthenticated-tabs menu-tabs))]
    [re-com/horizontal-tabs
     :src (at)
     :tabs tabs
     :model model
     :on-change (fn [tab-id]
                  (re-frame/dispatch [::events/navigate tab-id]))]))

(defn main-tab []
  (let [active-tab (re-frame/subscribe [::subs/active-tab])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :padding "1em"
     :children [[title]
                [menu]
                (routes/tabs @active-tab)]]))
