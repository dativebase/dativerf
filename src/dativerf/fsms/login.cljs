(ns dativerf.fsms.login)

(def state-machine
  {::is-ready
   {:dativerf.events/password-invalidated-login ::requires-password
    :dativerf.events/username-invalidated-login ::requires-username
    :dativerf.events/initiated-authentication ::is-authenticating
    :dativerf.events/no-op ::is-ready}
   ::requires-username
   {:dativerf.events/user-changed-username ::is-ready}
   ::requires-password
   {:dativerf.events/user-changed-password ::is-ready}
   ::is-invalid
   {:dativerf.events/user-changed-username ::is-ready
    :dativerf.events/user-changed-password ::is-ready
    :dativerf.events/user-changed-current-old-instance ::is-ready}
   ::user-is-authenticated
   {:dativerf.events/initiated-deauthentication ::is-deauthenticating}
   ::is-authenticating
   {:dativerf.events/server-not-authenticated ::is-invalid
    :dativerf.events/server-authenticated ::user-is-authenticated}
   ::is-deauthenticating
   {:dativerf.events/server-deauthenticated ::is-ready
    :dativerf.events/server-not-deauthenticated ::is-ready}})
