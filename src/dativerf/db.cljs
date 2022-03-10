(ns dativerf.db)

(def test-sample-olds
  [{:name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}
   {:id "run_mcgill_2022old"
    :name "Kirundi McGill 2022 OLD"
    :url "https://app.onlinelinguisticdatabase.org/run_mcgill_2022old"}])

(def default-db
  {:name "Dative"
   :active-tab :home
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
   :forms-paginator/last-page 1
   :forms-paginator/count 0
   :forms-paginator/first-form 0
   :forms-paginator/last-form 0})

(defn old [{:keys [old olds]}]
  (->> olds (filter (fn [{:keys [url]}] (= old url))) first))
