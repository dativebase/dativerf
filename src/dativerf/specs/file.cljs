(ns dativerf.specs.file
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]))

;; TODO: there are three distinct types of files, each with its own spec

;; TODO: use the following set of valid MIME types to spec ::mime-type?
(def allowed-file-mime-types
  #{"application/pdf"
    "image/gif"
    "image/jpeg"
    "image/png"
    "audio/mpeg"
    "audio/ogg"
    "audio/wav"
    "audio/x-wav"
    "video/mpeg"
    "video/mp4"
    "video/ogg"
    "video/quicktime"
    "video/x-ms-wmv"})

(s/def ::id ::common/id)
(s/def ::name (partial common/string-max-len? 255))
(s/def ::filename (partial common/string-max-len? 255))
(s/def ::mime-type (partial common/string-max-len? 255))
(s/def ::size nat-int?)
(s/def ::url (partial common/string-max-len? 255))
(s/def ::lossy-filename (partial common/string-max-len? 255))
(s/def ::mini-file (s/keys :req-un [::id
                                    ::name
                                    ::filename
                                    ::mime-type
                                    ::size
                                    ::url
                                    ::lossy-filename]))
(s/def ::mini-files (s/coll-of ::mini-file))
