(ns dativerf.subs
  (:require
   [re-frame.core :as re-frame]
   [dativerf.fsms.login :as login]
   [dativerf.models.application-settings :as application-settings]
   [dativerf.models.form :as form-model]
   [dativerf.models.old :as old-model]
   [dativerf.utils :as utils]))

(re-frame/reg-sub ::name (fn [db] (:name db)))
(re-frame/reg-sub ::old (fn [db] (:old db)))
(re-frame/reg-sub ::old-slug (fn [db] (old-model/slug db)))
(re-frame/reg-sub ::olds (fn [db] (->> db :olds (sort-by :name))))
(re-frame/reg-sub ::active-route (fn [db _] (:active-route db)))
(re-frame/reg-sub ::re-pressed-example (fn [db _] (:re-pressed-example db)))
(re-frame/reg-sub ::user (fn [db] (:user db)))

(re-frame/reg-sub
 ::old-name
 :<- [::old]
 :<- [::olds]
 (fn [[old-id olds] _] (old-model/name* {:old old-id :olds olds})))

(re-frame/reg-sub ::active-settings-tab (fn [db _] (:settings/active-tab db)))
(re-frame/reg-sub ::general-settings-edit-interface-visible?
                  (fn [db _] (:settings/general-edit-interface-visible? db)))
(re-frame/reg-sub ::input-validation-settings-edit-interface-visible?
                  (fn [db _] (:settings/input-validation-edit-interface-visible? db)))

(re-frame/reg-sub ::system-error (fn [db _] (:system/error db)))
(re-frame/reg-sub ::unicode-data (fn [db _] (:unicode-data db)))

(re-frame/reg-sub :login/username (fn [db] (:login/username db)))
(re-frame/reg-sub :login/password (fn [db] (:login/password db)))
(re-frame/reg-sub :login/state (fn [db _] (:login/state db)))
(re-frame/reg-sub :login/invalid-reason (fn [db _] (:login/invalid-reason db)))

;; "Edit Application Settings" interface subscriptions
(doseq [k application-settings/editable-keys]
  (re-frame/reg-sub
   (keyword "edited-settings" k)
   (fn [db] (application-settings/edited-setting db k))))

(defn edited-application-setting [db k]
  (get-in db [:settings/edited-settings k]
        ;; default to the most recent value fetched from the OLD
          (-> db
              (get-in [:old-states (:old db) :application-settings])
              application-settings/read-settings->write-settings
              k)))

(re-frame/reg-sub
 :settings/edited-settings-changed?
 (fn [db] (let [server-settings
                (application-settings/read-settings->write-settings
                 (get-in db [:old-states (:old db) :application-settings]))
                local-settings (application-settings/edited-settings db)]
            (not= local-settings server-settings))))

(re-frame/reg-sub ::languages
                  (fn [db]
                    (get-in db [:old-states (:old db) :languages :items])))

(re-frame/reg-sub ::mini-users
                  (fn [db]
                    (get-in db [:old-states (:old db) :mini-users :items])))

(re-frame/reg-sub ::mini-orthographies
                  (fn [db]
                    (get-in db [:old-states (:old db) :mini-orthographies :items])))

;; TODO: is this subscription needed any longer?
(re-frame/reg-sub ::settings-new-data-present?
                  (fn [db]
                    (let [{:keys [mini-users mini-orthographies languages]}
                          (get-in db [:old-states (:old db)])
                          users-fresh?
                          (and mini-users
                               (< (utils/seconds-ago (:dative/fetched-at mini-users))
                                  5))
                          orthographies-fresh?
                          (and mini-orthographies
                               (< (utils/seconds-ago (:dative/fetched-at mini-orthographies))
                                  5))]
                      (and languages users-fresh? orthographies-fresh?))))

;; "New Form" interface subscriptions
(re-frame/reg-sub :new-form/narrow-phonetic-transcription
                  (fn [db] (:new-form/narrow-phonetic-transcription db)))
(re-frame/reg-sub :new-form/phonetic-transcription
                  (fn [db] (:new-form/phonetic-transcription db)))
(re-frame/reg-sub :new-form/transcription (fn [db] (:new-form/transcription db)))
(re-frame/reg-sub :new-form/grammaticality (fn [db] (:new-form/grammaticality db)))
(re-frame/reg-sub :new-form/morpheme-break (fn [db] (:new-form/morpheme-break db)))
(re-frame/reg-sub :new-form/morpheme-gloss (fn [db] (:new-form/morpheme-gloss db)))
(re-frame/reg-sub :new-form/translations (fn [db] (:new-form/translations db)))
(re-frame/reg-sub :new-form/translation-transcription
                  (fn [db [_ index]]
                    (get-in db [:new-form/translations index :transcription])))
(re-frame/reg-sub :new-form/translation-grammaticality
                  (fn [db [_ index]]
                    (get-in db [:new-form/translations index :grammaticality])))
(re-frame/reg-sub :new-form/comments (fn [db] (:new-form/comments db)))
(re-frame/reg-sub :new-form/speaker-comments
                  (fn [db] (:new-form/speaker-comments db)))
(re-frame/reg-sub :new-form/elicitation-method
                  (fn [db] (:new-form/elicitation-method db)))
(re-frame/reg-sub :new-form/tags (fn [db] (:new-form/tags db)))
(re-frame/reg-sub :new-form/syntactic-category
                  (fn [db] (:new-form/syntactic-category db)))
(re-frame/reg-sub :new-form/date-elicited
                  (fn [db] (:new-form/date-elicited db)))
(re-frame/reg-sub :new-form/speaker (fn [db] (:new-form/speaker db)))
(re-frame/reg-sub :new-form/elicitor (fn [db] (:new-form/elicitor db)))
(re-frame/reg-sub :new-form/verifier (fn [db] (:new-form/verifier db)))
(re-frame/reg-sub :new-form/source (fn [db] (:new-form/source db)))
(re-frame/reg-sub :new-form/syntax (fn [db] (:new-form/syntax db)))
(re-frame/reg-sub :new-form/semantics (fn [db] (:new-form/semantics db)))
(re-frame/reg-sub :new-form/status (fn [db] (:new-form/status db)))

(re-frame/reg-sub
 ::old-settings
 (fn [db _]
   (get-in db [:old-states (:old db) :application-settings])))

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

(re-frame/reg-sub :new-form/state (fn [db _] (:new-form-state db)))
(re-frame/reg-sub
 :new-form/general-validation-error-message
 (fn [db _] (or (:new-form-general-validation-error-message db)
                form-model/new-form-validation-message)))

(re-frame/reg-sub
 :new-form/field-specific-validation-error-message
 (fn [db [_ field]] (-> db
                        :new-form-field-specific-validation-error-messages
                        field)))

(re-frame/reg-sub :edited-settings/state
                  (fn [db _] (:settings/edited-settings-state db)))
(re-frame/reg-sub
 :settings-edit/field-specific-validation-error-message
 (fn [db [_ field]] (-> db
                        :edited-settings-field-specific-validation-error-messages
                        field)))

;; Forms Browse Navigation Subscriptions
(re-frame/reg-sub ::forms-items-per-page
                  (fn [db _] (:forms-paginator/items-per-page db)))
(re-frame/reg-sub ::forms-current-page-forms
                  (fn [db _] (:forms-paginator/current-page-forms db)))
(re-frame/reg-sub ::forms-current-page
                  (fn [db _] (:forms-paginator/current-page db)))
(re-frame/reg-sub ::forms-last-page
                  (fn [db _] (:forms-paginator/last-page db)))
(re-frame/reg-sub ::forms-count
                  (fn [db _] (:forms-paginator/count db)))
(re-frame/reg-sub ::forms-first-form
                  (fn [db _] (:forms-paginator/first-form db)))
(re-frame/reg-sub ::forms-last-form
                  (fn [db _] (:forms-paginator/last-form db)))

(re-frame/reg-sub ::forms-force-reload?
                  (fn [db _] (:forms/force-reload? db)))
(re-frame/reg-sub ::forms-previous-route
                  (fn [db _] (:forms/previous-route db)))
(re-frame/reg-sub ::forms-labels-on?
                  (fn [db _] (:forms/labels-on? db)))
(re-frame/reg-sub ::old-settings-previous-route
                  (fn [db _] (:old-settings/previous-route db)))

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

(defn- form-view-state [db form-id]
  (get-in db [:old-states (:old db) :forms/view-state form-id]))

(re-frame/reg-sub ::form-expanded?
                  (fn [db [_ form-id]]
                    (-> db (form-view-state form-id) :expanded?)))

(re-frame/reg-sub ::form-export-interface-visible?
                  (fn [db [_ form-id]]
                    (-> db (form-view-state form-id) :export-interface-visible?)))

(re-frame/reg-sub ::form-export-format
                  (fn [db [_ form-id]]
                    (-> db (form-view-state form-id) :export-format)))

(re-frame/reg-sub ::forms-export-format
                  (fn [db _] (:forms/export-format db)))

(re-frame/reg-sub ::visible-form-fields
                  (fn [db _] (:forms/visible-fields db)))

(re-frame/reg-sub
 ::forms-export-interface-visible?
 (fn [db _] (:forms/export-interface-visible? db)))

(re-frame/reg-sub
 ::forms-settings-interface-visible?
 (fn [db _] (:forms/settings-interface-visible? db)))

(re-frame/reg-sub
 ::forms-settings-field-visibility-interface-visible?
 (fn [db _] (:forms/settings-field-visibility-interface-visible? db)))

(re-frame/reg-sub
 ::forms-new-form-interface-visible?
 (fn [db _] (:forms/new-form-interface-visible? db)))

(re-frame/reg-sub
 ::form-to-delete
 (fn [db _] (:forms/form-to-delete db)))

(re-frame/reg-sub
 ::forms-new-form-secondary-fields-visible?
 (fn [db _] (:forms/new-form-secondary-fields-visible? db)))

(re-frame/reg-sub
 ::forms-new
 (fn [db _] (get-in db [:old-states (:old db) :forms-new])))
