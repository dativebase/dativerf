(ns dativerf.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [dativerf.events :as events]
   [dativerf.routes :as routes]
   [dativerf.views :as views]
   [dativerf.config :as config]
   day8.re-frame.http-fx))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-tab] root-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/fetch-olds])
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (dev-setup)
  (mount-root))
