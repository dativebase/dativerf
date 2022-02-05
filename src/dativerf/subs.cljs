(ns dativerf.subs
  (:require
   [re-frame.core :as re-frame]
   [dativerf.fsms.login :as login]))

(re-frame/reg-sub ::name (fn [db] (:name db)))
(re-frame/reg-sub ::old (fn [db] (:old db)))
(re-frame/reg-sub ::olds (fn [db] (:olds db)))
(re-frame/reg-sub ::active-panel (fn [db _] (:active-panel db)))
(re-frame/reg-sub ::re-pressed-example (fn [db _] (:re-pressed-example db)))

(re-frame/reg-sub :login/username (fn [db] (:login/username db)))
(re-frame/reg-sub :login/password (fn [db] (:login/password db)))
(re-frame/reg-sub :login/state (fn [db _] (:login/state db)))
(re-frame/reg-sub :login/invalid-reason (fn [db _] (:login/invalid-reason db)))

;; TODO: this should be a function of :login/state but I don't know why/when we would want to disable the login inputs ...
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
 :login/logout-button-disabled?
 :<- [:login/state]
 (fn [login-state _] (not= login-state ::login/user-is-authenticated)))
