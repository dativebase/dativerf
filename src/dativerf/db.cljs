(ns dativerf.db)

(def test-sample-olds
  [{:name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}])

(def default-new-form-state
  {:new-form/narrow-phonetic-transcription {:val "" :visible? false}
   :new-form/phonetic-transcription {:val "" :visible? false}
   :new-form/transcription {:val ""}
   :new-form/grammaticality {:val ""}
   :new-form/morpheme-break {:val ""}
   :new-form/morpheme-gloss {:val ""}
   :new-form/translations {:val [{:transcription "" :grammaticality ""}]}
   :new-form/comments {:val ""}
   :new-form/speaker-comments {:val ""}
   :new-form/elicitation-method {:val nil}
   :new-form/tags {:val #{}}
   :new-form/syntactic-category {:val nil}
   :new-form/date-elicited {:val nil}
   :new-form/speaker {:val nil}
   :new-form/elicitor {:val nil}
   :new-form/verifier {:val nil :visible? false}
   :new-form/source {:val nil}
   :new-form/syntax {:val ""}
   :new-form/semantics {:val ""}
   :new-form/status {:val "tested"}})

(def default-new-form-vals
  (->> default-new-form-state
       (map (juxt key (comp :val val)))
       (into {})))

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
      (merge default-new-form-vals)))

(defn default-form-view-state [{:as _db :keys [forms/expanded?]}]
  {:expanded? expanded?
   :export-interface-visible? false
   :export-format :plain-text})

(defn old [{:keys [old olds]}]
  (->> olds (filter (fn [{:keys [url]}] (= old url))) first))

(defn old-slug [db] (:slug (old db)))
(defn old-name [db] (:name (old db)))
