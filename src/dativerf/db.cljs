(ns dativerf.db)

(def test-sample-olds
  [{:id "blaold"
    :name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:id "okaold"
    :name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}
   {:id "run_mcgill_2022old"
    :name "Kirundi McGill 2022 OLD"
    :url "https://app.onlinelinguisticdatabase.org/run_mcgill_2022old"}])


(def default-db
  {:name "Dative"
   :active-panel :home-panel
   :old (-> test-sample-olds first :id)
   ;; :olds []
   :olds test-sample-olds
   :user nil
   ;; login state
   :login/username ""
   :login/password ""
   :login/state :dativerf.fsms.login/is-ready
   :login/invalid-reason nil})
