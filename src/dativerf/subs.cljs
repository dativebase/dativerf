(ns dativerf.subs
  (:require
   [dativerf.db :as db]
   [dativerf.fsms.login :as login]
   [dativerf.models.application-settings :as application-settings]
   [dativerf.models.form :as form-model]
   [dativerf.models.old :as old-model]
   [dativerf.utils :as utils]
   [re-frame.core :as re-frame]))

(defn- form-view-state [db form-id]
  (get-in db [:old-states (:old db) :forms/view-state form-id]))

(doseq [[subscription path]
        {::active-route [:active-route]
         ::active-settings-tab [:settings/active-tab]
         :edited-settings/state [:settings/edited-settings-state]
         ::form-to-delete [:forms/form-to-delete]
         ::forms-count [:forms-paginator/count]
         ::forms-current-page [:forms-paginator/current-page]
         ::forms-current-page-forms [:forms-paginator/current-page-forms]
         ::forms-export-format [:forms/export-format]
         ::forms-export-interface-visible? [:forms/export-interface-visible?]
         ::forms-first-form [:forms-paginator/first-form]
         ::forms-force-reload? [:forms/force-reload?]
         ::forms-items-per-page [:forms-paginator/items-per-page]
         ::forms-labels-on? [:forms/labels-on?]
         ::forms-last-form [:forms-paginator/last-form]
         ::forms-last-page [:forms-paginator/last-page]
         ::forms-new-form-interface-visible? [:forms/new-form-interface-visible?]
         ::forms-new-form-secondary-fields-visible? [:forms/new-form-secondary-fields-visible?]
         ::forms-previous-route [:forms/previous-route]
         ::forms-settings-field-visibility-interface-visible? [:forms/settings-field-visibility-interface-visible?]
         ::forms-settings-interface-visible? [:forms/settings-interface-visible?]
         ::general-settings-edit-interface-visible? [:settings/general-edit-interface-visible?]
         ::input-validation-settings-edit-interface-visible? [:settings/input-validation-edit-interface-visible?]
         :login/invalid-reason [:login/invalid-reason]
         :login/password [:login/password]
         :login/state [:login/state]
         :login/username [:login/username]
         ::name [:name]
         :new-form/state [:new-form-state]
         ::old [:old]
         ::old-settings-previous-route [:old-settings/previous-route]
         ::re-pressed-example [:re-pressed-example]
         ::system-error [:system/error]
         ::unicode-data [:unicode-data]
         ::user [:user]
         ::visible-form-fields [:forms/visible-fields]}]
  (re-frame/reg-sub subscription (fn [db] (get-in db path))))

(doseq [subscription-key
        [:new-form/comments
         :new-form/date-elicited
         :new-form/elicitation-method
         :new-form/elicitor
         :new-form/grammaticality
         :new-form/morpheme-break
         :new-form/morpheme-gloss
         :new-form/narrow-phonetic-transcription
         :new-form/phonetic-transcription
         :new-form/semantics
         :new-form/source
         :new-form/speaker
         :new-form/speaker-comments
         :new-form/status
         :new-form/syntactic-category
         :new-form/syntax
         :new-form/tags
         :new-form/transcription
         :new-form/translations
         :new-form/verifier]]
  (re-frame/reg-sub subscription-key (fn [db] (subscription-key db))))

(doseq [[subscription new-form-translations-key]
        {:new-form/translation-grammaticality :grammaticality
         :new-form/translation-transcription :transcription}]
  (re-frame/reg-sub
   subscription
   (fn [db [_ index]]
     (get-in db [:new-form/translations index new-form-translations-key]))))

(doseq [[subscription mini-resource]
        {::grammaticalities :grammaticalities
         ::languages :languages
         ::mini-elicitation-methods :mini-elicitation-methods
         ::mini-orthographies :mini-orthographies
         ::mini-sources :mini-sources
         ::mini-speakers :mini-speakers
         ::mini-syntactic-categories :mini-syntactic-categories
         ::mini-tags :mini-tags
         ::mini-users :mini-users}]
  (re-frame/reg-sub
   subscription
   (fn [db] (get-in db [:old-states (:old db) mini-resource :items]))))

(doseq [[subscription resource]
        {::forms-new :forms-new
         ::old-settings :application-settings}]
  (re-frame/reg-sub
   subscription
   (fn [db] (get-in db [:old-states (:old db) resource]))))

(re-frame/reg-sub ::old-slug (fn [db] (old-model/slug db)))
(re-frame/reg-sub ::olds (fn [db] (->> db :olds (sort-by :name))))

(re-frame/reg-sub
 ::old-name
 :<- [::old]
 :<- [::olds]
 (fn [[old-id olds] _] (old-model/name* {:old old-id :olds olds})))

;; "Edit Application Settings" interface subscriptions
(doseq [k application-settings/editable-keys]
  (re-frame/reg-sub
   (keyword "edited-settings" k)
   (fn [db] (application-settings/edited-setting db k))))

(re-frame/reg-sub
 :settings/edited-settings-changed?
 (fn [db] (let [server-settings
                (application-settings/read-settings->write-settings
                 (get-in db [:old-states (:old db) :application-settings]))
                local-settings (application-settings/edited-settings db)]
            (not= local-settings server-settings))))

;; TODO: these should be a function of :login/state and :settings/state but I
;; don't know why/when we would want to disable these inputs ...
(re-frame/reg-sub :login/inputs-disabled? (constantly false))

(re-frame/reg-sub
 :login/username-status
 :<- [:login/state]
 :<- [:login/invalid-reason]
 (fn [[login-state invalid-reason] _]
   (case login-state
     ::login/requires-username [:error "Username required"]
     ::login/is-invalid [:error invalid-reason]
     ::login/user-is-authenticated [:success]
     ::login/is-authenticating [:validating]
     [nil])))

(re-frame/reg-sub
 :login/password-status
 :<- [:login/state]
 :<- [:login/invalid-reason]
 (fn [[login-state invalid-reason] _]
   (case login-state
     ::login/requires-password [:error "Password required"]
     ::login/is-invalid [:error invalid-reason]
     ::login/user-is-authenticated [:success]
     ::login/is-authenticating [:validating]
     [nil])))

(re-frame/reg-sub
 :login/login-button-disabled?
 :<- [:login/state]
 (fn [login-state _] (not= login-state ::login/is-ready)))

(re-frame/reg-sub
 :login/logged-in?
 :<- [:login/state]
 (fn [login-state _] (= login-state ::login/user-is-authenticated)))

(re-frame/reg-sub
 :new-form/general-validation-error-message
 (fn [db _] (or (:new-form-general-validation-error-message db)
                form-model/new-form-validation-message)))

(re-frame/reg-sub
 ::form-edit-general-validation-error-message
 (fn [db [_ form-id]]
   (or (some-> db (form-view-state form-id)
               :edit-general-validation-error-message)
       form-model/new-form-validation-message)))

(doseq [[subscription field-specific-validation-messages-key]
        {:new-form/field-specific-validation-error-message :new-form-field-specific-validation-error-messages
         :settings-edit/field-specific-validation-error-message :edited-settings-field-specific-validation-error-messages}]
  (re-frame/reg-sub
   subscription
   (fn [db [_ field]] (-> db
                          field-specific-validation-messages-key
                          field))))

(re-frame/reg-sub ::form-by-id
                  (fn [db [_ form-id]]
                    (get-in db [:old-states (:old db) :forms form-id])))

(re-frame/reg-sub ::form-by-int-id
                  (fn [db [_ form-id]]
                    (->> (get-in db [:old-states (:old db) :forms])
                         vals
                         (filter (fn [{:keys [id]}] (= form-id id)))
                         first)))

(re-frame/reg-sub ::forms-by-ids
                  (fn [db [_ form-ids]]
                    (select-keys
                     (get-in db [:old-states (:old db) :forms])
                     form-ids)))

(doseq [[subscription key]
        {::form-expanded? :expanded?
         ::form-export-interface-visible? :export-interface-visible?
         ::form-export-format :export-format
         ::form-edit-interface-visible? :edit-interface-visible?
         ::form-edit-secondary-fields-visible? :edit-secondary-fields-visible?
         ::form-edit-state :edit-fsm-state}]
  (re-frame/reg-sub
   subscription
   (fn [db [_ form-id]]
     (-> db (form-view-state form-id) key))))

(re-frame/reg-sub
 ::form-edit-field-specific-validation-error-messages
 (fn [db [_ field form-id]]
   (-> db
       (form-view-state form-id)
       :edit-field-specific-validation-error-messages
       field)))

(doseq [key form-model/editable-keys]
  (re-frame/reg-sub
   (keyword "edit-form" key)
   (fn [db [_ form-id]] (-> db (form-view-state form-id) :edit-state key))))

(re-frame/reg-sub
 ::edit-form-form-changed?
 (fn [db [_ form-id]]
   (let [server-form
         (-> db
             (get-in [:old-states (:old db) :forms form-id])
             form-model/read-form->write-form
             (update :date-elicited form-model/stringify-date))
         local-form (form-model/edit-form db form-id)]
     (not= local-form server-form))))

(re-frame/reg-sub
 ::new-form-form-changed?
 (fn [db _]
   (not= (form-model/new-form db)
         (utils/remove-namespaces-recursive db/default-new-form-state))))

(doseq [key [:grammaticality
             :transcription]]
  (re-frame/reg-sub
   (keyword "edit-form" (str "translation-" (name key)))
   (fn [db [_ index form-id]]
     (get-in (form-view-state db form-id)
             [:edit-state :translations index key]))))
