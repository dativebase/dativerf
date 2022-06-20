(ns dativerf.subs
  (:require
   [re-frame.core :as re-frame]
   [dativerf.db :as db]
   [dativerf.fsms.login :as login]))

(re-frame/reg-sub ::name (fn [db] (:name db)))
(re-frame/reg-sub ::old (fn [db] (:old db)))
(re-frame/reg-sub ::old-slug (fn [db] (-> db db/old :slug)))
(re-frame/reg-sub ::olds (fn [db] (->> db :olds (sort-by :name))))
(re-frame/reg-sub ::active-route (fn [db _] (:active-route db)))
(re-frame/reg-sub ::re-pressed-example (fn [db _] (:re-pressed-example db)))
(re-frame/reg-sub ::user (fn [db] (:user db)))

(re-frame/reg-sub ::active-settings-tab (fn [db _] (:settings/active-tab db)))

(re-frame/reg-sub :login/username (fn [db] (:login/username db)))
(re-frame/reg-sub :login/password (fn [db] (:login/password db)))
(re-frame/reg-sub :login/state (fn [db _] (:login/state db)))
(re-frame/reg-sub :login/invalid-reason (fn [db _] (:login/invalid-reason db)))

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
