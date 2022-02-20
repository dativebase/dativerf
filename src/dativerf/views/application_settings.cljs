(ns dativerf.views.application-settings
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.routes :as routes]
            [dativerf.subs :as subs]
            [dativerf.utils :as utils]
            [dativerf.views.widgets :as widgets]))

(defn- title []
  [re-com/title
   :src   (at)
   :label "Application Settings"
   :level :level2])

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
  (let [settings @(re-frame/subscribe [::subs/application-settings])]
    [re-com/v-box
     :src   (at)
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
  (let [settings @(re-frame/subscribe [::subs/application-settings])]
    [re-com/v-box
     :src   (at)
     :children
     (for [{:as row :keys [key]} server-settings-rows]
       ^{:key key} [widgets/key-value-row
                    (utils/kebab->space (name key))
                    (assoc row :value (key settings))])]))

(defmulti settings-subtabs identity)
(defmethod settings-subtabs :server [] [server-settings-subtab])
(defmethod settings-subtabs :input-validation [] [input-validation-subtab])

(def ^:private submenu-tabs
  [{:id :server
    :label "Server Settings"
    :authenticated? true}
   {:id :input-validation
    :label "Input Validation"
    :authenticated? true}])

(defn- menu []
  [re-com/horizontal-tabs
   :src (at)
   :tabs submenu-tabs
   :model @(re-frame/subscribe [::subs/active-settings-tab])
   :on-change (fn [tab-id]
                (re-frame/dispatch [::events/set-settings-active-tab tab-id])
                (re-frame/dispatch [::events/navigate tab-id]))])

(defn- settings-tab []
  [re-com/v-box
   :src (at)
   :gap "1em"
   :padding "1em"
   :children
   [[title]
    [menu]
    (settings-subtabs @(re-frame/subscribe [::subs/active-settings-tab]))]])

(defmethod routes/tabs :application-settings [] [settings-tab])
