(ns dativerf.fsms.new-form)

(def state-machine
  {::ready
   {:dativerf.events/new-form-data-invalid ::invalid
    :dativerf.events/initiated-form-creation ::creating
    :dativerf.events/new-form-no-op ::ready}
   ::invalid
   {:dativerf.events/user-changed-new-form-data ::ready}
   ::creating
   {:dativerf.events/form-not-created ::invalid
    :dativerf.events/form-created ::ready}})
