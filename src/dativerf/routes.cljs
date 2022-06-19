(ns dativerf.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [dativerf.events :as events]))

(def routes
  (atom
   ["/" {"" :home
         "login" :login
         [:old]
         {"/collections" :collections
          "/forms"
          {"" :forms-last-page
           "/page/"
           {[:page]
            {"/items-per-page/"
             {[:items-per-page] :forms-page}}}
           "/" {[:id] :form-page}}
          "/files" :files
          "/logout" :logout
          "/settings" :settings}
         "application-settings" :application-settings}]))

(defmulti tabs :handler)

(defmethod tabs :default [route]
  [:div (str "No tab found for this route: " route)])

(defn parse [url]
  (bidi/match-route @routes url))

(defn serialize [{:keys [handler route-params]}]
  (apply bidi/path-for (into [@routes handler] (flatten (seq route-params)))))

(defn dispatch [route]
  (re-frame/dispatch [::events/set-active-route route]))

(defonce history (pushy/pushy dispatch parse))

(defn navigate! [route]
  (pushy/set-token! history (serialize route)))

(defn start! []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
