(ns dativerf.views.old-settings
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.db :as db]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]))

(defn- title []
  (let [old-id @(re-frame/subscribe [::subs/old])
        olds @(re-frame/subscribe [::subs/olds])
        old-name (db/old-name {:old old-id :olds olds})]
    [re-com/title
     :src   (at)
     :label (str old-name " Settings")
     :level :level2]))

(def input-validation-rows
  [{:key :orthographic-validation :type :string}
   {:key :storage-orthography :type :string}
   {:key :narrow-phonetic-validation :type :string}
   {:key :narrow-phonetic-inventory :type :string}
   {:key :broad-phonetic-validation :type :string}
   {:key :broad-phonetic-inventory :type :string}
   {:key :morpheme-break-validation :type :string}
   {:key :morpheme-break-is-orthographic :type :boolean}
   {:key :phonemic-inventory :type :string}
   {:key :morpheme-delimiters :type :string}
   {:key :punctuation :type :character-sequence-as-string}
   {:key :grammaticalities :type :string}])

(defn- input-validation-subtab []
  (let [settings @(re-frame/subscribe [::subs/old-settings])]
    [re-com/v-box
     :src (at)
     :children
     (for [{:as row :keys [key]} input-validation-rows]
       ^{:key key} [widgets/key-value-row
                    (utils/kebab->space (name key))
                    (assoc row :value (key settings))])]))

(def server-settings-rows
  [{:key :object-language-name :type :string}
   {:key :object-language-id :type :string}
   {:key :metalanguage-name :type :string}
   {:key :metalanguage-id :type :string}
   {:key :unrestricted-users :type :coll-of-users}
   {:key :datetime-modified :type :string}])

(defn- server-settings-subtab []
  (let [settings @(re-frame/subscribe [::subs/old-settings])]
    [re-com/v-box
     :src (at)
     :children
     (for [{:as row :keys [key]} server-settings-rows]
       ^{:key key} [widgets/key-value-row
                    (utils/kebab->space (name key))
                    (assoc row :value (key settings))])]))

(defmulti settings-subtabs :handler)

(defmethod settings-subtabs :old-settings []
  [server-settings-subtab])

(defmethod settings-subtabs :old-settings-input-validation []
  [input-validation-subtab])

(def ^:private submenu-tabs
  [{:id :old-settings
    :label "General Settings"}
   {:id :old-settings-input-validation
    :label "Input Validation"}])

(defn- menu [{:keys [handler]}]
  [re-com/horizontal-tabs
   :src (at)
   :tabs submenu-tabs
   :model handler
   :on-change (fn [tab-id]
                (re-frame/dispatch
                 [::events/navigate
                  {:handler tab-id
                   :route-params {:old @(re-frame/subscribe [::subs/old-slug])}}]))])

(defn- settings-tab [route]
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[title]
    [menu route]
    (settings-subtabs route)]])

(defn- old-settings [route]
  (if @(re-frame/subscribe [::subs/old-settings])
    [settings-tab route]
    (do (re-frame/dispatch [::events/fetch-applicationsettings])
        [re-com/throbber :size :large])))

(defmethod routes/tabs :old-settings [route]
  (old-settings route))

(defmethod routes/tabs :old-settings-input-validation [route]
  (old-settings route))
