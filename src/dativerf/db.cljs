(ns dativerf.db)

(def test-sample-olds
  [{:name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}])

(def default-new-form-state
  {:new-form/transcription ""
   :new-form/grammaticality ""
   :new-form/morpheme-break ""
   :new-form/morpheme-gloss ""
   :new-form/translations [{:transcription "" :grammaticality ""}]
   :new-form/comments ""
   :new-form/speaker-comments ""
   :new-form/elicitation-method nil
   :new-form/tags #{}
   :new-form/syntactic-category nil
   :new-form/date-elicited nil
   :new-form/speaker nil
   :new-form/elicitor nil
   :new-form/source nil
   :new-form/syntax ""
   :new-form/semantics ""})

(def default-db
  (-> {:name "Dative"
       :active-route {:handler :home}
       :olds []
       :old nil
       :old-states {}
       :user nil
       ;; settings state
       :settings/active-tab :server
       ;; login state
       :login/username ""
       :login/password ""
       :login/state :dativerf.fsms.login/is-ready
       :login/invalid-reason nil
       ;; forms browse state
       :forms-paginator/items-per-page 10
       :forms-paginator/current-page-forms []
       :forms-paginator/current-page 1
       :forms-paginator/last-page nil
       :forms-paginator/count 0
       :forms-paginator/first-form 0
       :forms-paginator/last-form 0
       :forms/labels-on? false
       :forms/expanded? false
       :forms/export-interface-visible? false
       :forms/export-format :plain-text
       :forms/new-form-interface-visible? false
       :forms/new-form-secondary-fields-visible? false
       ;; routing state
       :forms/previous-route nil
       :forms/previous-browse-route nil
       :old-settings/previous-route nil}
      (merge default-new-form-state)))

(defn old [{:keys [old olds]}]
  (->> olds (filter (fn [{:keys [url]}] (= old url))) first))

(defn old-slug [db] (:slug (old db)))
(defn old-name [db] (:name (old db)))
