(ns dativerf.db)

(def test-sample-olds
  [{:name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}])

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
   :login/invalid-reason nil})

(defn old [{:keys [old olds]}]
  (->> olds (filter (fn [{:keys [url]}] (= old url))) first))
