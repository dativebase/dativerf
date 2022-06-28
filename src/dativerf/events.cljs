(ns dativerf.events
  (:require
   [ajax.core :as ajax]
   [clojure.string :as str]
   [cljs.pprint :as pprint]
   [dativerf.db :as db]
   [dativerf.fsms :as fsms]
   [dativerf.fsms.login :as login]
   [dativerf.fsms.new-form :as new-form-fsm]
   [dativerf.models.old :as old-model]
   [dativerf.models.form :as form-model]
   [dativerf.old :as old]
   [dativerf.specs.form :as form-specs]
   [dativerf.utils :as utils]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]))

(def app-dative-servers-url "https://app.dative.ca/servers.json")
(def max-forms-in-memory 200)

(defn- warn [msg] (println (str "WARNING: " msg)))

(defn- register-toggler
  "Handle event by toggling boolean in db at path"
  [event path]
  (re-frame/reg-event-db event (fn-traced [db _] (update-in db path not))))

(defn- register-form-toggler
  "Handle event by toggling form-specific boolean in db at path generated by
   path-fn"
  [event path-fn]
  (re-frame/reg-event-db
   event
   (fn [db [_ form-id]] (update-in db (path-fn db form-id) not))))

(doseq [[event path]
        {::user-clicked-forms-labels-button
         [:forms/labels-on?]
         ::user-clicked-toggle-secondary-new-form-fields
         [:forms/new-form-secondary-fields-visible?]
         ::user-clicked-export-forms-button
         [:forms/export-interface-visible?]
         ::user-clicked-form-settings-button
         [:forms/settings-interface-visible?]
         ::user-clicked-toggle-form-field-visibility-interface
         [:forms/settings-field-visibility-interface-visible?]
         ::user-clicked-new-form-button
         [:forms/new-form-interface-visible?]}]
  (register-toggler event path))

(def get-request
  {:method :get
   :format (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keywords? true})
   :with-credentials true
   :cookie-policy :standard})

(def post-request (assoc get-request :method :post))

(defn- forms->uuid-keyed-forms-map [forms fetched-at]
  (->> forms
       (map (comp
             ;; TODO: fetched-at should be in view state, not in the data itself.
             #(assoc % :dative/fetched-at fetched-at)
             form-specs/parse-form))
       (map (juxt :uuid identity))
       (into {})))

(defn- forms->view-states [forms db]
  (let [default-form-view-state (db/default-form-view-state db)]
    (->> forms
         keys
         (map (juxt
               identity
               (constantly default-form-view-state)))
         (into {}))))

(defn- reformat-forms-response
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
        forms (forms->uuid-keyed-forms-map items fetched-at)
        forms-view-state (forms->view-states forms db)]
    {:forms forms
     :forms-view-state forms-view-state
     :paginator {:forms-paginator/items-per-page items-per-page
                 :forms-paginator/current-page-forms (mapv :uuid (sort-by :id (vals forms)))
                 :forms-paginator/current-page current-page
                 :forms-paginator/last-page last-page
                 :forms-paginator/count count
                 :forms-paginator/first-form first-form
                 :forms-paginator/last-form last-form}}))

(defn- prune-forms
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

(re-frame/reg-event-db ::initialize-db (fn-traced [_ _] db/default-db))

(re-frame/reg-event-fx
 ::navigate
 (fn-traced [{:keys [db]} [_ route]]
            {:navigate route
             :db (cond-> db
                   (utils/forms-route? route)
                   (assoc :forms/previous-route route)
                   (utils/forms-browse-route? route)
                   (assoc :forms/previous-browse-route route)
                   (utils/old-settings-route? route)
                   (assoc :old-settings/previous-route route))}))

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
            (assoc db :olds (old-model/olds-response->olds olds))))

(re-frame/reg-event-db
 ::olds-not-fetched
 (fn-traced [db [_ response]]
            (warn "failed to fetch the olds from the server!")
            (prn response)
            db))

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
                             [[:shortcut/old-settings]
                              [{:keyCode 188
                                :ctrlKey true
                                :shiftKey true}]] ;; C-,
                             [[:shortcut/new-form]
                              [{:keyCode 65
                                :ctrlKey true
                                :shiftKey true}]]] ;; C-A
                :clear-keys
                [[{:keyCode 27}]]

                :always-listen-keys
                [{:keyCode 72 :ctrlKey true :shiftKey true}   ;; C-H
                 {:keyCode 76 :ctrlKey true :shiftKey true}   ;; C-L
                 {:keyCode 66 :ctrlKey true :shiftKey true}   ;; C-B
                 {:keyCode 70 :ctrlKey true :shiftKey true}   ;; C-F
                 {:keyCode 73 :ctrlKey true :shiftKey true}   ;; C-I
                 {:keyCode 188 :ctrlKey true :shiftKey true}  ;; C-,
                 {:keyCode 65 :ctrlKey true :shiftKey true}]  ;; C-A
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
                       :route-params {:old (old-model/slug db)}}]
                     [::navigate {:handler :login}])]]}))

(re-frame/reg-event-fx
 :shortcut/forms
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               (or (:forms/previous-route db)
                                   {:handler :forms-last-page
                                    :route-params {:old (old-model/slug db)}})]]]})))

(re-frame/reg-event-fx
 :shortcut/new-form
 (fn-traced [{{:keys [user]} :db} _]
            (when user {:fx [[:dispatch [::user-clicked-new-form-button]]]})))

(re-frame/reg-event-fx
 :shortcut/files
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               {:handler :files
                                :route-params {:old (old-model/slug db)}}]]]})))

(re-frame/reg-event-fx
 :shortcut/collections
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               {:handler :collections
                                :route-params {:old (old-model/slug db)}}]]]})))

(re-frame/reg-event-fx
 :shortcut/old-settings
 (fn-traced [{{:keys [user] :as db} :db} _]
            (when user {:fx [[:dispatch
                              [::navigate
                               (or (:old-settings/previous-route db)
                                   {:handler :old-settings
                                    :route-params {:old (old-model/slug db)}})]]]})))

;; New Form Form Events

(defn- cache-new-form [db]
  (let [new-form (form-model/new-form db)]
    (assoc db
           :new-form new-form
           :new-form-field-specific-validation-error-messages
           (form-model/new-form-field-specific-validation-error-messages new-form))))

(defn- invalidate-new-form-cache [db]
    (assoc db
           :new-form nil
           :new-form-general-validation-error-message nil
           :new-form-field-specific-validation-error-messages {}))

(defn-traced transition-new-form-fsm [db event]
  (fsms/update-state db new-form-fsm/state-machine :new-form-state event))

(defn-traced transition-new-form-fsm-db-handler [db [event]]
  (cond-> db
    :always
    (transition-new-form-fsm event)
    (= ::new-form-data-invalid event)
    cache-new-form))

(doseq [event [::new-form-data-invalid
               ::no-op]]
  (re-frame/reg-event-db event transition-new-form-fsm-db-handler))

;; Login Page/Form Events

(defn-traced transition-login-fsm [db event]
  (fsms/update-state db login/state-machine :login/state event))

(defn-traced transition-login-fsm-db-handler [db [event]]
  (transition-login-fsm db event))

(doseq [event [::username-invalidated-login
               ::password-invalidated-login
               ::no-op]]
  (re-frame/reg-event-db event transition-login-fsm-db-handler))

(re-frame/reg-event-db
 ::user-selected-visible-form-fields
 (fn-traced [db [_ visible-form-fields]]
            (assoc db :forms/visible-fields visible-form-fields)))

(re-frame/reg-event-db
 ::user-clicked-make-all-form-fields-visible-button
 (fn-traced [db _]
            (assoc db :forms/visible-fields (set (keys db/default-form-state)))))

(re-frame/reg-event-db
 ::user-clicked-make-no-form-fields-visible-button
 (fn-traced [db _] (assoc db :forms/visible-fields db/always-visible-form-fields)))

(re-frame/reg-event-db
 ::user-clicked-restore-default-field-visibility-button
 (fn-traced [db _]
            (assoc db :forms/visible-fields db/default-visible-form-fields)))

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
 ::user-clicked-back-to-browse-button
 (fn-traced [{:keys [db]} _]
            {:fx [[:dispatch [::navigate
                              (or (:forms/previous-browse-route db)
                                  {:handler :forms-last-page
                                   :route-params {:old (old-model/slug db)}})]]]}))

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

;; Network Requests

(re-frame/reg-event-fx
 ::initiated-authentication
 (fn-traced [{:keys [db]} [event _]]
            (let [{:keys [login/username login/password]} db]
              {:db (fsms/update-state db login/state-machine :login/state event)
               :http-xhrio
               (assoc post-request
                      :uri (old/login-authenticate (old-model/old db))
                      :params {:username username :password password}
                      :on-success [::server-authenticated]
                      :on-failure [::server-not-authenticated])})))

(re-frame/reg-event-fx
 ::initiated-deauthentication
 (fn-traced [{:keys [db]} [event _]]
            {:db (fsms/update-state db login/state-machine :login/state event)
             :http-xhrio
             (assoc get-request
                    :method :get
                    :uri (old/login-logout (old-model/old db))
                    :on-success [::server-deauthenticated]
                    :on-failure [::server-not-deauthenticated])}))

(re-frame/reg-event-fx
 ::initiated-form-creation
 (fn-traced [{:keys [db]} [event]]
            {:db (transition-new-form-fsm db event)
             :http-xhrio
             (assoc post-request
                    :uri (old/forms (old-model/old db))
                    :params (utils/->snake-case-recursive
                             (form-model/new-form db))
                    :on-success [::form-created]
                    :on-failure [::form-not-created])}))

(re-frame/reg-event-db
 ::turn-off-force-forms-reload
 (fn-traced [db _] (assoc db :forms/force-reload? false)))

(re-frame/reg-event-fx
 ::form-created
 (fn-traced [{:keys [db]} [event _response]]
            {:db (-> db
                     (transition-new-form-fsm event)
                     invalidate-new-form-cache
                     (merge db/default-new-form-state)
                     (assoc :forms/force-reload? true))
             ;; TODO: Is always navigating to the last page of forms the best behaviour?
             :fx [[:dispatch [::navigate
                              {:handler :forms-last-page
                               :route-params
                               {:old (old-model/slug db)}}]]]}))

(defn- format-server-validation-errors
  "Unfortunately, the OLD's validation responses are not very regular. We
  regularize them here."
  [errors]
  (if (string? errors)
    {:new-form-general-validation-error-message errors}
    {:new-form-field-specific-validation-error-messages
     (->> (utils/->kebab-case-recursive errors)
          (map (juxt key (comp (fn [v] (if (coll? v)
                                         (first v)
                                         v)) val)))
          (into {}))}))

(re-frame/reg-event-db
 ::form-not-created
 (fn-traced [db [event {:as r :keys [status] {:keys [errors]} :response}]]
            ;; TODO: this when indicates a non-validation error response from
            ;; the OLD. We should inform the user of this so they can retry, or
            ;; we should retry.
            (when-not (and errors (= 400 status))
              (warn "OLD server returned an unexpected response to a POST /forms request:")
              (pprint/pprint r))
            (when (and errors (= 400 status))
              (pprint/pprint
               (format-server-validation-errors errors)))
            (cond-> db
              :always
              (transition-new-form-fsm event)
              (and errors (= 400 status))
              (merge (format-server-validation-errors errors)))))

(re-frame/reg-event-fx
 ::fetch-form
 (fn-traced [{:keys [db]} [_ form-id]]
            {:http-xhrio
             (assoc get-request
                    :uri (old/form (old-model/old db) form-id)
                    :on-success [::form-fetched]
                    :on-failure [::form-not-fetched form-id])}))

(re-frame/reg-event-fx
 ::fetch-forms-page
 (fn-traced [{:keys [db]} [_ page items-per-page]]
            {:http-xhrio
             (assoc get-request
                    :uri (-> db old-model/old old/forms)
                    :params {:page page
                             :items_per_page items-per-page}
                    :on-success [::forms-page-fetched]
                    :on-failure [::forms-page-not-fetched
                                 page items-per-page])}))

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
                      :uri (-> db old-model/old old/forms)
                      :params {:page page
                               :items_per_page items-per-page}
                      :on-success [::forms-first-page-for-last-page-fetched]
                      :on-failure [::forms-page-not-fetched
                                   page items-per-page])})))

;; If the first page is also the last page we just merge the forms into the DB.
;; Otherwise, we navigate to the last forms page, which will trigger an
;; unambiguous page request for the last page.
(re-frame/reg-event-fx
 ::forms-first-page-for-last-page-fetched
 (fn [{:keys [db]} [_ response]]
   (let [{:keys [forms forms-view-state paginator]}
         (reformat-forms-response response db)
         last-page (:forms-paginator/last-page paginator)
         items-per-page (:forms-paginator/items-per-page paginator)
         route {:handler :forms-page
                :route-params
                {:old (old-model/slug db)
                 :items-per-page items-per-page
                 :page last-page}}
         fx {:db (-> db
                     (update-in [:old-states (:old db) :forms] merge forms)
                     (update-in [:old-states (:old db) :forms/view-state]
                                merge forms-view-state)
                     (merge paginator))}]
     (if (= 1 last-page)
       fx
       (assoc fx :fx [[:dispatch [::navigate route]]])))))

(re-frame/reg-event-fx
 ::fetch-new-form-data
 (fn-traced [{:keys [db]} _]
            {:http-xhrio
             (assoc get-request
                    :uri (old/forms-new (old-model/old db))
                    :on-success [::forms-new-fetched]
                    :on-failure [::forms-new-not-fetched])}))

(re-frame/reg-event-fx
 ::fetch-applicationsettings
 (fn-traced [{:keys [db]} _]
            {:http-xhrio
             (assoc get-request
                    :uri (old/applicationsettings (old-model/old db))
                    :on-success [::applicationsettings-fetched]
                    :on-failure [::applicationsettings-not-fetched])}))

;; Network Success Events

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
                                 {:old (old-model/slug db)}}]]]}
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
 ::forms-new-fetched
 (fn-traced [db [_event forms-new]]
             (assoc-in
              db
              [:old-states (:old db) :forms-new]
              (utils/->kebab-case-recursive forms-new))))

(re-frame/reg-event-db
  ::formsearches-new-fetched
  (fn-traced [db [_event formsearches-new]]
             (assoc-in
              db
              [:old-states (:old db) :formsearches-new]
              (utils/->kebab-case-recursive formsearches-new))))

(re-frame/reg-event-fx
 ::server-deauthenticated
 (fn-traced [{:keys [db]} [event _]]
            {:db (-> db
                     (assoc :user nil)
                     (update :old-states
                             (fn [old-states] (dissoc old-states (:old db))))
                     (transition-login-fsm event))
             :fx [[:dispatch [::navigate {:handler :login}]]]}))

(re-frame/reg-event-db
 ::form-fetched
 (fn-traced [db [_event response]]
            (let [form (utils/->kebab-case-recursive response)
                  fetched-at (.now js/Date)
                  forms (forms->uuid-keyed-forms-map [form] fetched-at)
                  forms-view-state (forms->view-states forms db)]
              (-> db
                  (update-in [:old-states (:old db) :forms] merge forms)
                  (update-in [:old-states (:old db) :forms/view-state]
                             merge forms-view-state)
                  prune-forms))))

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

;; Network Failure Events
;; TODO handle these failures better.

(re-frame/reg-event-db
 ::formsearches-new-not-fetched
 (fn-traced [db [_event _]]
            (warn "failed to fetch formsearches/new for this OLD")
            db))

(re-frame/reg-event-db
 ::forms-page-not-fetched
 (fn-traced [db [_ page items-per-page]]
            (warn (str "failed to fetch forms page " page
                       " with %s items per page " items-per-page))
            db))

(re-frame/reg-event-db
 ::form-not-fetched
 (fn-traced [db [_ form-id]]
            (warn (str "failed to fetch form " form-id))
            db))

(re-frame/reg-event-db
 ::server-not-authenticated
 (fn-traced [db [event {:keys [response]}]]
            (let [error-msg (:error response "Undetermined error.")]
              (-> db
                  (assoc :login/invalid-reason error-msg)
                  (fsms/update-state login/state-machine :login/state event)))))

(re-frame/reg-event-db
 ::applicationsettings-not-fetched
 (fn-traced [db [_event _response]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (warn "failed to fetch the applicationsettings for this OLD")
            db))

(re-frame/reg-event-db
 ::forms-new-not-fetched
 (fn-traced [db [_event _]]
            ;; TODO handle this failure better. Probably logout and alert the user.
            (warn "failed to fetch forms/new for this OLD")
            db))


(re-frame/reg-event-db
 ::server-not-deauthenticated
 (fn-traced [db [event _]]
            (warn "Failed to logout of OLD.")
            (-> db
                (assoc :user nil
                       :active-route {:handler :login})
                (update :old-states
                        (fn [old-states] (dissoc old-states (:old db))))
                (transition-login-fsm event))))

;; Forms Browse Navigation Events

(re-frame/reg-event-fx
 ::user-changed-items-per-page
 (fn [{:keys [db]} [_ items-per-page]]
            (let [first-form (:forms-paginator/first-form db)
                  current-page (Math/ceil (/ first-form items-per-page))
                  last-page (Math/ceil (/ (:forms-paginator/count db)
                                          items-per-page))
                  route {:handler :forms-page
                         :route-params
                         {:old (old-model/slug db)
                          :items-per-page items-per-page
                          :page current-page}}]
              {:db (assoc db
                          :forms-paginator/items-per-page items-per-page
                          :forms-paginator/current-page current-page
                          :forms-paginator/last-page last-page)
               :fx [[:dispatch [::navigate route]]]})))

;; "New Form" interface events

;; Register event handlers for all "New Form" interface events (except
;; translations, which are special)
(doseq [[k _] db/default-new-form-state
        :when (not= k :new-form/translations)]
  (re-frame/reg-event-db
   (keyword "dativerf.events" (str "user-changed-new-form-" (name k)))
   (fn-traced [db [_ v]]
              (-> db
                  (assoc k v)
                  (transition-new-form-fsm ::user-changed-new-form-data)
                  invalidate-new-form-cache))))

(doseq [k [:transcription :grammaticality]]
  (re-frame/reg-event-db
   (keyword "dativerf.events"
            (str "user-changed-new-form-translation-" (name k)))
   (fn-traced [db [_ i v]]
              (-> db
                  (assoc-in [:new-form/translations i k] v)
                  (transition-new-form-fsm ::user-changed-new-form-data)
                  invalidate-new-form-cache))))

(re-frame/reg-event-db
 ::user-clicked-add-new-translation-button
 (fn-traced [db _]
            (update db :new-form/translations conj {:transcription "" :grammaticality ""})))

(re-frame/reg-event-db
 ::user-clicked-remove-translation-button
 (fn-traced [db [_ index]]
            (update db :new-form/translations (fn [translations]
                                                (->> translations
                                                     (map vector (range))
                                                     (filter (fn [[idx]] (not= index idx)))
                                                     (map second)
                                                     vec)))))

(re-frame/reg-event-db
 ::user-clicked-clear-new-form-interface
 (fn-traced [db _] (merge db db/default-new-form-state)))

(re-frame/reg-event-db
 ::user-clicked-help-creating-new-form
 ;; TODO
 (fn-traced [_db _]))

(re-frame/reg-event-fx
 ::user-clicked-create-new-form-button
 (fn-traced [{:keys [db]} _]
            {:dispatch (if (form-specs/write-form-valid? (form-model/new-form db))
                         [::initiated-form-creation]
                         [::new-form-data-invalid])}))

(register-form-toggler
 ::user-clicked-export-form-button
 (fn [{:keys [old]} form-id]
   [:old-states old :forms/view-state form-id :export-interface-visible?]))

(register-form-toggler
::user-clicked-form
 (fn [{:keys [old]} form-id]
   [:old-states old :forms/view-state form-id :expanded?]))

(re-frame/reg-event-db
 ::user-selected-form-export
 (fn-traced [db [_ form-id export-id]]
            (assoc-in
             db
             [:old-states (:old db) :forms/view-state form-id :export-format]
             export-id)))

(re-frame/reg-event-db
 ::user-selected-forms-export
 (fn-traced [db [_ export-id]]
            (assoc db :forms/export-format export-id)))

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
