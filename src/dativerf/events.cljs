(ns dativerf.events
  (:require
   [ajax.core :as ajax]
   [clojure.string :as str]
   [dativerf.db :as db]
   [dativerf.fsms :as fsms]
   [dativerf.fsms.login :as login]
   [dativerf.models.old :as models-old]
   [dativerf.old :as old]
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
 (fn-traced [_ [_ handler]] {:navigate handler}))

(def app-dative-servers-url "https://app.dative.ca/servers.json")

(re-frame/reg-event-fx
 ::fetch-olds
 (fn-traced [_cofx _event]
            {:http-xhrio
             {:method :get
              :uri app-dative-servers-url
              :format (ajax/json-request-format)
              :response-format (ajax/json-response-format {:keywords? true})
              :on-success [::olds-fetched]
              :on-failure [::olds-not-fetched]}}))

(re-frame/reg-event-db
 ::olds-fetched
 (fn-traced [db [_ olds]]
            (assoc db :olds (models-old/olds-response->olds olds))))

(re-frame/reg-event-db
 ::olds-not-fetched
 (fn-traced [db [_ response]]
            (println "WARNING: failed to fetch the olds from the server!")
            (prn response)
            db))

(re-frame/reg-event-fx
 ::set-active-tab
 (fn-traced [{:keys [db]} [_ active-tab]]
   {:db (assoc db :active-tab active-tab)
    :dispatch [::rp/set-keydown-rules
               {:event-keys [[[:shortcut/home]
                              [{:keyCode 72}]] ;; h
                             [[:shortcut/login]
                              [{:keyCode 76}]] ;; l
                             [[:shortcut/forms]
                              [{:keyCode 66}]] ;; b
                             [[:shortcut/files]
                              [{:keyCode 70}]] ;; f
                             [[:shortcut/collections]
                              [{:keyCode 73}]] ;; i
                             ]
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
 (fn-traced [_ _] {:fx [[:dispatch [::navigate :home]]]}))

(re-frame/reg-event-fx
 :shortcut/login
 (fn-traced [{:keys [db]} _] {:fx [[:dispatch [::navigate (if (:user db)
                                                            :logout
                                                            :login)]]]}))

(re-frame/reg-event-fx
 :shortcut/forms
 (fn-traced [_cofx _] {:fx [[:dispatch [::navigate :forms]]]}))

(re-frame/reg-event-fx
 :shortcut/files
 (fn-traced [_cofx _] {:fx [[:dispatch [::navigate :files]]]}))

(re-frame/reg-event-fx
 :shortcut/collections
 (fn-traced [_cofx _] {:fx [[:dispatch [::navigate :collections]]]}))

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
            (let [{:keys [login/username login/password]} db]
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
            (let [{:keys [login/username login/password]} db]
              {:db (fsms/update-state db login/state-machine :login/state event)
               :http-xhrio
               {:method :post
                :uri (old/login-authenticate (db/old db))
                :with-credentials true
                :params {:username username :password password}
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::server-authenticated]
                :on-failure [::server-not-authenticated]}})))

(re-frame/reg-event-fx
 ::initiated-deauthentication
 (fn-traced [{:keys [db]} [event _]]
            {:db (fsms/update-state db login/state-machine :login/state event)
             :http-xhrio
             {:method :get
              :uri (old/login-logout (db/old db))
              :format (ajax/json-request-format)
              :with-credentials true
              :response-format (ajax/json-response-format {:keywords? true})
              :on-success [::server-deauthenticated]
              :on-failure [::server-not-deauthenticated]}}))

(re-frame/reg-event-fx
 ::server-authenticated
 (fn-traced [{:keys [db]} [event {:keys [authenticated user]}]]
            (if authenticated
              (let [old (db/old db)]
                {:db (-> db
                         (fsms/update-state login/state-machine :login/state event)
                         (assoc :user (utils/->kebab-case-recursive user)
                                :login/username ""
                                :login/password ""
                                :active-tab :forms))
                 :http-xhrio
                 [{:method :get
                   :uri (old/applicationsettings old)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :with-credentials true
                   :cookie-policy :standard
                   :on-success [::applicationsettings-fetched]
                   :on-failure [::applicationsettings-not-fetched]}
                  {:method :get
                   :uri (old/forms-new old)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :with-credentials true
                   :cookie-policy :standard
                   :on-success [::forms-new-fetched]
                   :on-failure [::forms-new-not-fetched]}
                  {:method :get
                   :uri (old/formsearches-new old)
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :with-credentials true
                   :cookie-policy :standard
                   :on-success [::formsearches-new-fetched]
                   :on-failure [::formsearches-new-not-fetched]}
                  {:method :get
                   :uri (old/forms old)
                   :params {:page 1 :items_per_page 10}
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :with-credentials true
                   :cookie-policy :standard
                   :on-success [::forms-page-1-fetched]
                   :on-failure [::forms-page-1-not-fetched]}]})
              {:db (-> db
                       (fsms/update-state login/state-machine :login/state
                                          ::server-not-authenticated)
                       (assoc :user nil))})))

(re-frame/reg-event-db
 ::applicationsettings-fetched
 (fn-traced [db [_event application-settings-entities]]
            (assoc-in
             db
             [:old-states (:old db) :application-settings]
             (utils/->kebab-case-recursive
              (last application-settings-entities)))))

(re-frame/reg-event-db
 ::applicationsettings-not-fetched
 (fn-traced [db [_event _response]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println "WARNING: failed to fetch the applicationsettings for this OLD")
            db))

(re-frame/reg-event-db
 ::forms-new-fetched
 (fn-traced [db [_event forms-new]]
             (assoc-in
              db
              [:old-states (:old db) :forms-new]
              (utils/->kebab-case-recursive forms-new))))

(re-frame/reg-event-db
 ::forms-new-not-fetched
 (fn-traced [db [_event _]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println "WARNING: failed to fetch forms/new for this OLD")
            db))

(re-frame/reg-event-db
  ::formsearches-new-fetched
  (fn-traced [db [_event formsearches-new]]
             (assoc-in
              db
              [:old-states (:old db) :formsearches-new]
              (utils/->kebab-case-recursive formsearches-new))))

(re-frame/reg-event-db
 ::formsearches-new-not-fetched
 (fn-traced [db [_event _]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println "WARNING: failed to fetch formsearches/new for this OLD")
            db))

(re-frame/reg-event-fx
 ::forms-page-1-fetched
 (fn [{:keys [db]} [_event forms-page-1]]
            (let [{{:keys [items-per-page count]} :paginator :as forms-page-1}
                  (utils/->kebab-case-recursive forms-page-1)
                  new-page (int (/ count items-per-page))]
              {:db (assoc-in db
                             [:old-states (:old db) :forms-page-1]
                             forms-page-1)
               :http-xhrio {:method :get
                            :uri (old/forms (db/old db))
                            :params {:page new-page :items_per_page items-per-page}
                            :format (ajax/json-request-format)
                            :response-format (ajax/json-response-format
                                              {:keywords? true})
                            :with-credentials true
                            :cookie-policy :standard
                            :on-success [::forms-page-n-fetched]
                            :on-failure [::forms-page-n-not-fetched]}})))

(re-frame/reg-event-db
 ::forms-page-1-not-fetched
 (fn-traced [db [_event _]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println "WARNING: failed to fetch forms page 1 for this OLD")
            db))

(re-frame/reg-event-db
 ::forms-page-n-fetched
 (fn-traced [db [_event forms-page-n]]
            (assoc-in db
                      [:old-states (:old db) :forms-page-n]
                      forms-page-n)))

(re-frame/reg-event-db
 ::forms-page-n-not-fetched
 (fn-traced [db [_event _]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println "WARNING: failed to fetch forms page N for this OLD")
            db))

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
                (assoc :user nil
                       :active-tab :login)
                (update :old-states
                        (fn [old-states] (dissoc old-states (:old db))))
                (transition-login-fsm event))))

(re-frame/reg-event-db
 ::server-not-deauthenticated
 (fn-traced [db [event _]]
            (println "WARNING: Failed to logout of OLD.")
            (-> db
                (assoc :user nil
                       :active-tab :login)
                (update :old-states
                        (fn [old-states] (dissoc old-states (:old db))))
                (transition-login-fsm event))))
