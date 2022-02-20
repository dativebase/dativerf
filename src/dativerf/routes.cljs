(ns dativerf.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [dativerf.events :as events]))

(defmulti tabs identity)
(defmethod tabs :default [route]
  [:div (str "No tab found for this route: " route)])

(def routes
  (atom
    ["/" {"" :home
          "login" :login
          "logout" :logout
          "forms" :forms
          "files" :files
          "collections" :collections
          "application-settings" :application-settings}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [tab (keyword (str (name (:handler route))))]
    (re-frame/dispatch [::events/set-active-tab tab])))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start! []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
