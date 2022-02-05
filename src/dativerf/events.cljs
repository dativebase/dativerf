(ns dativerf.events
  (:require
   [ajax.core :as ajax]
   [clojure.string :as str]
   [dativerf.db :as db]
   [dativerf.fsms :as fsms]
   [dativerf.fsms.login :as login]
   [dativerf.utils :as utils]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::navigate
 (fn-traced [_ [_ handler]]
            {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn-traced [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)
    :dispatch [::rp/set-keydown-rules
               {:event-keys [[[:shortcut/home]
                              [{:keyCode 72}]]
                             [[:shortcut/login]
                              [{:keyCode 76}]]]
                :clear-keys
                [[{:keyCode 27}]]}]}))

;; Cf. https://day8.github.io/re-frame/FAQs/FocusOnElement/
;; TODO: I was not able to get this to work.
(re-frame/reg-fx
 :focus-to-element
 (fn [element-id]
   (reagent/after-render
    (fn [& _]
      (some-> js/document (.getElementById element-id) .focus)))))

(re-frame/reg-event-fx
 :shortcut/home
 (fn-traced [cofx _] {:fx [[:dispatch [::navigate :home]]]}))

(re-frame/reg-event-fx
 :shortcut/login
 (fn-traced [cofx _] {:fx [[:dispatch [::navigate :login]]]}))

;; Login Page/Form Events

(defn-traced transition-login-fsm [db event]
  (fsms/update-state db login/state-machine :login/state event))

(defn-traced transition-login-fsm-db-handler [db [event _]]
  (transition-login-fsm db event))

(re-frame/reg-event-db ::username-invalidated-login
                       transition-login-fsm-db-handler)
(re-frame/reg-event-db ::password-invalidated-login
                       transition-login-fsm-db-handler)
(re-frame/reg-event-db ::no-op transition-login-fsm-db-handler)

(re-frame/reg-event-db
 ::user-changed-current-old-instance
 (fn-traced [db [event old]]
            (-> db
                (assoc :old old)
                (fsms/update-state login/state-machine :login/state event))))

(re-frame/reg-event-db
 ::user-changed-username
 (fn-traced [db [event username]]
            (-> db
                (assoc :login/username username)
                (fsms/update-state login/state-machine :login/state event))))

(re-frame/reg-event-db
 ::user-changed-password
 (fn-traced [db [event password]]
            (-> db
                (assoc :login/password password)
                (fsms/update-state login/state-machine :login/state event))))

(re-frame/reg-event-fx
 ::user-clicked-login
 (fn-traced [{:keys [db]} _]
            (let [{:keys [login/username login/password login/state]} db]
              {:db db
               :dispatch (cond (str/blank? username)
                               [::username-invalidated-login]
                               (str/blank? password)
                               [::password-invalidated-login]
                               :else
                               [::initiated-authentication])})))

(re-frame/reg-event-fx
 ::user-pressed-enter-in-login
 (fn-traced [{:keys [db]} _]
            (let [{:keys [login/username login/password login/state]} db]
              {:db db
               :dispatch (cond (str/blank? username)
                               [::username-invalidated-login]
                               (str/blank? password)
                               [::password-invalidated-login]
                               (= state ::login/is-ready)
                               [::initiated-authentication]
                               :else
                               [::no-op])})))

(re-frame/reg-event-fx
 ::user-clicked-logout
 (fn-traced [{:keys [db]} _]
            {:db db
             :dispatch [::initiated-deauthentication]}))

(re-frame/reg-event-fx
 ::initiated-authentication
 (fn-traced [{:keys [db]} [event _]]
            (let [{:keys [login/username login/password old olds]} db]
              {:db (fsms/update-state db login/state-machine :login/state event)
               :http-xhrio
               {:method :post
                :uri (str (->> olds (filter (fn [{:keys [id]}] (= old id)))
                               first :url)
                          "/login/authenticate")
                :params {:username username :password password}
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::server-authenticated]
                :on-failure [::server-not-authenticated]}})))

(re-frame/reg-event-fx
 ::initiated-deauthentication
 (fn-traced [{:keys [db]} [event _]]
            (let [{:keys [old olds]} db]
              {:db (fsms/update-state db login/state-machine :login/state event)
               :http-xhrio
               {:method :get
                :uri (str
                      (->> olds (filter (fn [{:keys [id]}] (= old id))) first :url)
                      "/login/logout")
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::server-deauthenticated]
                :on-failure [::server-not-deauthenticated]}})))

(re-frame/reg-event-db
 ::server-authenticated
 (fn-traced [db [event {:keys [authenticated user]}]]
            (if authenticated
              (-> db
                  (fsms/update-state login/state-machine :login/state event)
                  (assoc :user (utils/->kebab-case-recursive user)
                         :login/username ""
                         :login/password ""))
              (-> db
                  (fsms/update-state login/state-machine :login/state
                                     ::server-not-authenticated)
                  (assoc :user nil)))))

(re-frame/reg-event-db
 ::server-not-authenticated
 (fn-traced [db [event {:keys [response]}]]
            (let [error-msg (:error response "Undetermined error.")]
              (-> db
                  (assoc :login/invalid-reason error-msg)
                  (fsms/update-state login/state-machine :login/state event)))))

(re-frame/reg-event-db
 ::server-deauthenticated
 (fn-traced [db [event _]]
            (-> db
                (assoc :user nil)
                (transition-login-fsm event))))

(re-frame/reg-event-db
 ::server-not-deauthenticated
 (fn-traced [db [event _]]
            (println "WARNING: Failed to logout of OLD.")
            (-> db
                (assoc :user nil)
                (transition-login-fsm event))))
