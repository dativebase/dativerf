(ns dativerf.old
  (:require [clojure.string :as str]))

(defn url [{:keys [url]}]
  (if (str/ends-with? url "/")
    url
    (str url "/")))

(defn applicationsettings [old] (str (url old) "applicationsettings"))
(defn forms-new [old] (str (url old) "forms/new"))
(defn formsearches-new [old] (str (url old) "formsearches/new"))
(defn forms [old] (str (url old) "forms"))
(defn form [old id] (str (url old) "forms/" id))
(defn login-authenticate [old] (str (url old) "login/authenticate"))
(defn login-logout [old] (str (url old) "login/logout"))
