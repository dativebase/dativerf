(ns dativerf.views.home
  (:require
   [re-com.core :as re-com :refer [at]]
   [dativerf.routes :as routes]))

(defn home-title []
  [re-com/title
   :src   (at)
   :label "Home"
   :level :level2
   :class nil #_(styles/level1)])

(defn home-body []
  [re-com/v-box
   :src   (at)
   :children
   [[re-com/p "Welcome to Dative."]]])

(defn home-tab []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :padding  "1em"
   :children [[home-title]
              [home-body]]])

(defmethod routes/tabs :home [] [home-tab])
