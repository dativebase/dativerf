(ns dativerf.fsms.search)

(def state-machine
  {::is-ready
   {:dativerf.events/initiated-authentication ::is-authenticating
    :dativerf.events/no-op ::is-ready}
   ::requires-input
   {:dativerf.events/user-changed-search-input  ::is-ready}
   ::is-invalid
   {:dativerf.events/user-changed-search-input ::is-ready
    :dativerf.events/user-changed-current-old-instance ::is-ready}
   ::user-is-authenticated
   {:dativerf.events/initiated-deauthentication ::is-deauthenticating}
   ::is-authenticating
   {:dativerf.events/server-not-authenticated ::is-invalid
    :dativerf.events/server-authenticated ::user-is-authenticated}
   ::is-deauthenticating
   {:dativerf.events/server-deauthenticated ::is-ready
    :dativerf.events/server-not-deauthenticated ::is-ready}})
