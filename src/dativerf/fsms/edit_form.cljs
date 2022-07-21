(ns dativerf.fsms.edit-form)

(def state-machine
  {::ready
   {:dativerf.events/edit-form-data-invalid ::invalid
    :dativerf.events/initiated-form-update ::updating
    :dativerf.events/edit-form-no-op ::ready}
   ::invalid
   {:dativerf.events/user-changed-edit-form-data ::ready}
   ::updating
   {:dativerf.events/form-not-updated ::invalid
    :dativerf.events/form-updated ::ready}})
