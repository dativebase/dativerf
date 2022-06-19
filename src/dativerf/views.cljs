(ns dativerf.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.db :as db]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.styles :as styles]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            dativerf.views.application-settings
            dativerf.views.forms
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
  [{:id :home
    :label "Home"
    :old-specific? false
    :authenticated? nil}
   {:id :forms
    :label "Forms"
    :old-specific? true
    :authenticated? true}
   #_{:id :files :label "Files" :authenticated? true}
   #_{:id :collections :label "Collections" :authenticated? true}
   {:id :application-settings
    :label "Settings"
    :old-specific? false
    :authenticated? true}
   {:id :login
    :label "Login"
    :old-specific? false
    :authenticated? false}
   {:id :logout
    :label "Logout"
    :old-specific? true
    :authenticated? true}])

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
        {:as route :keys [handler]} @(re-frame/subscribe [::subs/active-route])
        tab (get utils/handler->tab handler handler)
        old @(re-frame/subscribe [::subs/old])
        olds @(re-frame/subscribe [::subs/olds])
        authenticated? (boolean user)
        tabs (if authenticated?
               (authenticated-tabs menu-tabs)
               (unauthenticated-tabs menu-tabs))
        tab (if (some #{tab} (map :id tabs)) tab :home)]
    [re-com/horizontal-tabs
     :src (at)
     :tabs tabs
     :model tab
     :on-change
     (fn [tab-id]
       (re-frame/dispatch
        (let [[tab-map] (for [tm tabs :when (= tab-id (:id tm))] tm)
              forms-previous-route @(re-frame/subscribe [::subs/forms-previous-route])
              handler (get utils/tab->handler tab-id tab-id)
              route
              (cond (and (= :forms tab-id)
                         forms-previous-route)
                    forms-previous-route
                    (:old-specific? tab-map)
                    {:handler (get utils/tab->handler tab-id tab-id)
                     :route-params
                     {:old (:slug (db/old {:old old :olds olds}))}}
                    :else
                    {:handler (get utils/tab->handler tab-id tab-id)})]
          [::events/navigate route])))]))

(defn main-tab []
  (let [active-route (re-frame/subscribe [::subs/active-route])]
    [re-com/v-box
     :src (at)
     :height "100%"
     :max-width "800px"
     :padding "1em"
     :children [[title]
                [menu]
                (routes/tabs @active-route)]]))
