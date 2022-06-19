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

(def get-request
  {:method :get
   :format (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keywords? true})
   :with-credentials true
   :cookie-policy :standard})

(defn default-form-view-state [{:keys [forms/expanded?]}]
  {:expanded? expanded?})

(defn reformat-forms-response
  "Reformat a GET /forms response from an OLD:
  - Make it all kebab-case.
  - Make a forms map from form UUIDs to form maps with fetched-at timestamps.
  - Create a forms-view-state map from form UUIDs to view state about each form.
  - Create a representation of the paginator. It contains the (current) page,
    the items per page, the count of forms in the OLD, the last logical page,
    the indices of the first and last forms in the current page, and the UUIDs
    of all forms in the current page.
  - Return a map containing the forms, the forms view state and the paginator
    implied by the response."
  [response db]
  (let [{:keys [items paginator]} (utils/->kebab-case-recursive response)
        {:keys [page items-per-page count]} paginator
        current-page page
        last-page (Math/ceil (/ count items-per-page))
        first-form (- (* current-page items-per-page)
                      (dec items-per-page))
        last-form (min count (+ first-form (dec items-per-page)))
        fetched-at (.now js/Date)
        forms (->> items
                   (map (juxt :uuid
                              (fn [form]
                                (assoc form
                                       :dative/fetched-at fetched-at))))
                   (into {}))
        default-form-view-state* (default-form-view-state db)
        forms-view-state (->> forms
                              keys
                              (map (juxt
                                    identity
                                    (constantly
                                     default-form-view-state*)))
                              (into {}))]
    {:forms forms
     :forms-view-state forms-view-state
     :paginator {:forms-paginator/items-per-page items-per-page
                 :forms-paginator/current-page-forms (mapv :uuid items)
                 :forms-paginator/current-page current-page
                 :forms-paginator/last-page last-page
                 :forms-paginator/count count
                 :forms-paginator/first-form first-form
                 :forms-paginator/last-form last-form}}))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::navigate
 (fn-traced [_ [_ route]] {:navigate route}))

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

(re-frame/reg-event-db
 ::set-settings-active-tab
 (fn-traced [db [_ active-settings-tab]]
            (assoc db :settings/active-tab active-settings-tab)))

(re-frame/reg-event-fx
 ::set-active-route
 (fn-traced [{:keys [db]} [_ active-route]]
   {:db (assoc db :active-route active-route)
    ;; WARNING: dispatching the following here means that each route change
    ;; resets the keyboard shortcuts vacuously. This may seem wasteful, but I
    ;; suspect we will want view-specific shortcuts so I am leaving it here as-is
    ;; on purpose.
    :dispatch [::rp/set-keydown-rules
               {:event-keys [[[:shortcut/home]
                              [{:keyCode 72
                                :ctrlKey true
                                :shiftKey true}]] ;; C-H
                             [[:shortcut/login]
                              [{:keyCode 76
                                :ctrlKey true
                                :shiftKey true}]] ;; C-L
                             [[:shortcut/forms]
                              [{:keyCode 66
                                :ctrlKey true
                                :shiftKey true}]] ;; C-B
                             #_[[:shortcut/files]
                              [{:keyCode 70
                                :ctrlKey true
                                :shiftKey true}]] ;; C-F
                             #_[[:shortcut/collections]
                              [{:keyCode 73
                                :ctrlKey true
                                :shiftKey true}]] ;; C-I
                             [[:shortcut/application-settings]
                              [{:keyCode 188
                                :ctrlKey true
                                :shiftKey true}]] ;; C-,
                             ]
                :clear-keys
                [[{:keyCode 27}]]

                :always-listen-keys
                [{:keyCode 72 :ctrlKey true :shiftKey true}   ;; C-H
                 {:keyCode 76 :ctrlKey true :shiftKey true}   ;; C-L
                 {:keyCode 66 :ctrlKey true :shiftKey true}   ;; C-B
                 {:keyCode 70 :ctrlKey true :shiftKey true}   ;; C-F
                 {:keyCode 73 :ctrlKey true :shiftKey true}   ;; C-I
                 {:keyCode 188 :ctrlKey true :shiftKey true}] ;; C-,
                }]}))

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
 (fn-traced [_ _] {:fx [[:dispatch [::navigate {:handler :home}]]]}))

(re-frame/reg-event-fx
 :shortcut/login
 (fn-traced [{:keys [db]} _]
            {:fx [[:dispatch
                   (if (:user db)
                     [::navigate
                      {:handler :logout
                       :route-params {:old (:slug (db/old db))}}]
                     [::navigate {:handler :login}])]]}))

(re-frame/reg-event-fx
 :shortcut/forms
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               (or (:forms/previous-route db)
                                   {:handler :forms-last-page
                                    :route-params {:old (:slug (db/old db))}})]]]})))

(re-frame/reg-event-fx
 :shortcut/files
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               {:handler :files
                                :route-params {:old (:slug (db/old db))}}]]]})))

(re-frame/reg-event-fx
 :shortcut/collections
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               {:handler :collections
                                :route-params {:old (:slug (db/old db))}}]]]})))

(re-frame/reg-event-fx
 :shortcut/application-settings
 (fn-traced [{{:keys [user]} :db} _]
            (when user {:fx [[:dispatch [::navigate
                                         {:handler :application-settings}]]]})))

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
 ::fetch-forms-page
 (fn-traced [{:keys [db]} [_ page items-per-page]]
            (let [route {:handler :forms-page
                         :route-params
                         {:old (:slug (db/old db))
                          :items-per-page items-per-page
                          :page page}}]
              {:db (assoc db :forms/previous-route route)
               :http-xhrio
               (assoc get-request
                      :uri (-> db db/old old/forms)
                      :params {:page page
                               :items_per_page items-per-page}
                      :on-success [::forms-page-fetched]
                      :on-failure [::forms-page-not-fetched
                                   page items-per-page])})))

;; Context:
;; To get the last page of an OLD's forms set, it is necessary to first make a
;; request for the first page. This gives us the count of forms and lets us use
;; the pagination parameters in the OLD's REST API do determine the correct
;; request for the final page of forms.
(re-frame/reg-event-fx
 ::fetch-forms-last-page
 (fn-traced [{:keys [db]} _]
            (let [page 1
                  items-per-page (:forms-paginator/items-per-page db)]
              {:http-xhrio
               (assoc get-request
                      :uri (-> db db/old old/forms)
                      :params {:page page
                               :items_per_page items-per-page}
                      :on-success [::forms-first-page-for-last-page-fetched]
                      :on-failure [::forms-page-not-fetched
                                   page items-per-page])})))

(re-frame/reg-event-fx
 ::forms-first-page-for-last-page-fetched
 (fn [{:keys [db]} [_ response]]
   (let [{:keys [forms forms-view-state paginator]}
         (reformat-forms-response response db)
         last-page (:forms-paginator/last-page paginator)
         items-per-page (:forms-paginator/items-per-page paginator)
         route {:handler :forms-page
                :route-params
                {:old (:slug (db/old db))
                 :items-per-page items-per-page
                 :page last-page}}]
     (let [fx {:db (-> db
                       (update-in [:old-states (:old db) :forms] merge forms)
                       (update-in [:old-states (:old db) :forms/view-state]
                                  merge forms-view-state)
                       (merge paginator)
                       (assoc :forms/previous-route route))}]
       (if (= 1 last-page)
         fx
         (assoc fx :fx [[:dispatch [::navigate route]]]))))))

(re-frame/reg-event-fx
 ::server-authenticated
 (fn-traced [{:keys [db]} [event {:keys [authenticated user]}]]
            (if authenticated
              {:db (-> db
                       (fsms/update-state login/state-machine :login/state event)
                       (assoc :user (utils/->kebab-case-recursive user)
                              :login/username ""
                              :login/password ""))
               :fx [[:dispatch [::navigate
                                {:handler :forms-last-page
                                 :route-params
                                 {:old (:slug (db/old db))}}]]]}
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

(re-frame/reg-event-db
 ::forms-page-not-fetched
 (fn-traced [db [_ page items-per-page]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (println
             "WARNING: failed to fetch forms page"
             page
             "with %s items per page"
             items-per-page)
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
                       :active-route {:handler :login})
                (update :old-states
                        (fn [old-states] (dissoc old-states (:old db))))
                (transition-login-fsm event))))

(re-frame/reg-event-db
 ::server-not-deauthenticated
 (fn-traced [db [event _]]
            (println "WARNING: Failed to logout of OLD.")
            (-> db
                (assoc :user nil
                       :active-route {:handler :login})
                (update :old-states
                        (fn [old-states] (dissoc old-states (:old db))))
                (transition-login-fsm event))))

;; Forms Browse Navigation Events

(defn get-forms-page-request []
  (assoc get-request
         :on-success [::forms-page-fetched]
         :on-failure [::forms-page-not-fetched]))

(def max-forms-in-memory 200)

(defn prune-forms
  "Remove the oldest forms from the DB. We only keep the max-forms-in-memory
  newest forms."
  [db]
  (let [forms (get-in db [:old-states (:old db) :forms])
        form-count (count forms)
        to-drop (max 0 (- form-count max-forms-in-memory))]
    (if (zero? to-drop)
      db
      (let [forms (->> forms
                       vals
                       (sort-by :dative/fetched-at)
                       (drop to-drop)
                       (map (juxt :uuid identity))
                       (into {}))
            forms-view-state (-> db
                                 (get-in [:old-states (:old db)
                                          :forms/view-state])
                                 (select-keys (keys forms)))]
        (-> db
            (assoc-in [:old-states (:old db) :forms] forms)
            (assoc-in [:old-states (:old db) :forms/view-state]
                      forms-view-state))))))

(re-frame/reg-event-db
 ::forms-page-fetched
 (fn-traced [db [_event response]]
            (let [{:keys [forms forms-view-state paginator]}
                  (reformat-forms-response response db)]
              (-> db
                  (update-in [:old-states (:old db) :forms] merge forms)
                  (update-in [:old-states (:old db) :forms/view-state]
                             merge forms-view-state)
                  prune-forms
                  (merge paginator)))))

(re-frame/reg-event-db
 ::user-clicked-forms-labels-button
 (fn-traced [db _] (update db :forms/labels-on? not)))

(re-frame/reg-event-fx
 ::user-changed-items-per-page
 (fn [{:keys [db]} [_ items-per-page]]
            (let [first-form (:forms-paginator/first-form db)
                  current-page (Math/ceil (/ first-form items-per-page))
                  last-page (Math/ceil (/ (:forms-paginator/count db)
                                          items-per-page))
                  route {:handler :forms-page
                         :route-params
                         {:old (:slug (db/old db))
                          :items-per-page items-per-page
                          :page current-page}}]
              {:db (assoc db
                          :forms-paginator/items-per-page items-per-page
                          :forms-paginator/current-page current-page
                          :forms-paginator/last-page last-page
                          :forms/previous-route route)
               :fx [[:dispatch [::navigate route]]]})))

(re-frame/reg-event-db
 ::user-clicked-form
 (fn-traced [db [_event form-id]]
            (update-in
             db
             [:old-states (:old db) :forms/view-state form-id :expanded?]
             not)))

(re-frame/reg-event-db
 ::user-clicked-form
 (fn-traced [db [_event form-id]]
            (update-in
             db
             [:old-states (:old db) :forms/view-state form-id :expanded?]
             not)))

(defn- set-forms-expanded [forms-view-state expanded?]
  (->> forms-view-state
       (map (juxt key
                  (comp
                   (fn [form-view-state]
                     (assoc form-view-state :expanded? expanded?))
                   val)))
       (into {})))

(re-frame/reg-event-db
 ::user-clicked-collapse-all-forms-button
 (fn-traced [db _]
            (-> db
                (assoc :forms/expanded? false)
                (update-in
                 [:old-states (:old db) :forms/view-state]
                 set-forms-expanded false))))

(re-frame/reg-event-db
 ::user-clicked-expand-all-forms-button
 (fn-traced [db _]
            (-> db
                (assoc :forms/expanded? true)
                (update-in
                 [:old-states (:old db) :forms/view-state]
                 set-forms-expanded true))))
