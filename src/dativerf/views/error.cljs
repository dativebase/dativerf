(ns dativerf.views.error
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com :refer [at]]
            [dativerf.events :as events]
            [dativerf.subs :as subs]))

(def generic-advice
  "Try navigating to the home page and then retry what you were doing.
  If that doesn't work, try reloading the page.")

(def invalid-application-settings
  (str "Sadly the application settings fetched from the OLD do not appear to be"
       " valid. " generic-advice))

(def errors
  {:default
   (str "Oops! An unidentified error occurred. " generic-advice)
   :fetch-first-page-forms-retries-exceeded
   (str "Uh-oh! Dative tried three times to fetch the first page of forms from"
        " this OLD and all three attempts failed! " generic-advice)
   :fetch-first-page-forms-failed
   (str "Fiddlesticks! Dative failed to fetch the first page of forms from this"
        " OLD! " generic-advice)
   :form-not-fetched
   (str "Shucks! We were unable to fetch that form for you!" generic-advice)
   :authenticate-retries-exceeded
   (str "Rats! Dative tried three times to authenticate to the OLD with your"
        " credentials but all three attempts failed! " generic-advice)
   :authenticate-failed
   (str "Tarnation! Your attempt to authenticated failed in an unexpected"
        " manner." generic-advice)
   :application-settings-invalid-by-spec invalid-application-settings
   :application-settings-invalid invalid-application-settings
   :unicode-data-not-fetched
   (str "Sadly Dative was unable to fetch the Unicode data that allows it to"
        " name the characters used in orthographies and inventories. "
        generic-advice)})

(defn modal []
  (when-let [system-error @(re-frame/subscribe [::subs/system-error])]
    [re-com/modal-panel
     :src (at)
     :backdrop-color "grey"
     :backdrop-opacity 0.4
     :backdrop-on-click (fn [] (re-frame/dispatch
                                [::events/disregard-system-error]))
     :child
     [re-com/v-box
      :max-width "800px"
      :children
      [[re-com/alert-box
        :alert-type :danger
        :heading "System Error"
        :body (get errors system-error (:default errors))]
       [re-com/h-box
        :gap "10px"
        :justify :end
        :children
        [[re-com/button
          :label "Ok"
          :tooltip "dismiss the error"
          :attr {:auto-focus true
                 :on-key-up (fn [e]
                              (when (= "Escape" (.-key e))
                                (re-frame/dispatch
                                 [::events/disregard-system-error])))}
          :on-click (fn [_] (re-frame/dispatch
                              [::events/disregard-system-error]))]]]]]]))
