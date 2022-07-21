(ns dativerf.fsms.edit-settings)

(def state-machine
  {::ready
   {:dativerf.events/edited-settings-data-invalid ::invalid
    :dativerf.events/initiated-settings-update ::updating
    :dativerf.events/edited-settings-no-op ::ready}
   ::invalid
   {:dativerf.events/user-changed-edited-settings-data ::ready}
   ::updating
   {:dativerf.events/settings-not-updated ::invalid
    :dativerf.events/settings-updated ::ready}})
