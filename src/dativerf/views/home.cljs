(ns dativerf.views.home
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [dativerf.styles :as styles]
   [dativerf.events :as events]
   [dativerf.routes :as routes]
   [dativerf.subs :as subs]))

;; home

(defn display-re-pressed-example []
  (let [re-pressed-example (re-frame/subscribe [::subs/re-pressed-example])]
    [:div

     [:p
      [:span "Re-pressed is listening for keydown events. A message will be displayed when you type "]
      [:strong [:code "hello"]]
      [:span ". So go ahead, try it out!"]]

     (when-let [rpe @re-pressed-example]
       [re-com/alert-box
        :src        (at)
        :alert-type :info
        :body       rpe])]))

(defn home-title []
  (let [name @(re-frame/subscribe [::subs/name])
        old-id @(re-frame/subscribe [::subs/old])
        olds @(re-frame/subscribe [::subs/olds])
        old-name (or (some->> olds
                              (filter (fn [o] (= old-id (:id o))))
                              first
                              :name) "Dative")]
    [re-com/title
     :src   (at)
     :label old-name
     :level :level1
     :class (styles/level1)]))

(defn link-to-login-page []
  [re-com/hyperlink
   :src      (at)
   :label    "Login"
   :on-click #(re-frame/dispatch [::events/navigate :login])])

(defn home-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :padding  "1em"
   :children [[home-title]
              [link-to-login-page]
              [display-re-pressed-example]]])

(defmethod routes/panels :home-panel [] [home-panel])
