(ns dativerf.fsms.login)

(def state-machine
  {::ready
   {:dativerf.events/search-input-invalid ::invalid
    :dativerf.events/initiated-search ::searching
    :dativerf.events/no-op ::ready}
   ::invalid
   {:dativerf.events/user-changed-search-input ::ready}
   ::creating
   {:dativerf.events/forms-not-searched ::invalid
    :dativerf.events/form-searched ::ready}})
