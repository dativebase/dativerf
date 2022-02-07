(ns dativerf.db)

(def test-sample-olds
  [{:id "blaold"
    :name "Blackfoot OLD"
    :url "https://app.onlinelinguisticdatabase.org/blaold"}
   {:id "okaold"
    :name "Okanagan OLD"
    :url "https://app.onlinelinguisticdatabase.org/okaold"}])

(def default-db
  {:name "Dative"
   :active-tab :home
   ;; :olds []
   :old (-> test-sample-olds first :id)
   :olds test-sample-olds
   :old-states {}
   :user nil
   :application-settings nil
   ;; login state
   :login/username ""
   :login/password ""
   :login/state :dativerf.fsms.login/is-ready
   :login/invalid-reason nil})

(defn old [{:keys [old olds]}]
  (->> olds (filter (fn [{:keys [id]}] (= old id))) first))
