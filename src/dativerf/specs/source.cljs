(ns dativerf.specs.source
  (:require [clojure.spec.alpha :as s]
            [dativerf.specs.common :as common]
            [dativerf.specs.file :as file]))

;; All types of sources (both "mini" and regular) should have the following keys.
(s/def ::id ::common/id) ;; 3,
(s/def ::crossref (partial common/string-max-len? 1000))
(s/def ::crossref-source (s/nilable ::mini-source))
(s/def ::crossref-source-id (s/nilable ::common/id))
(s/def ::type #{"article"
                "book"
                "booklet"
                "conference"
                "inbook"
                "incollection"
                "inproceedings"
                "manual"
                "mastersthesis"
                "misc"
                "phdthesis"
                "proceedings"
                "techreport"
                "unpublished"})
(s/def ::key (s/and string?
                    (partial
                     re-find
                     #"^[0-9a-zA-Z!\"'#\$%&\(\)\^\*+\./:;<=>\?@\[\]\{\}\\_`\|~-]{1,1000}$")))
(s/def ::journal (partial common/string-max-len? 255))
(s/def ::editor (partial common/string-max-len? 255))
(s/def ::chapter (partial common/string-max-len? 255))
(s/def ::pages (partial common/string-max-len? 100))
(s/def ::publisher (partial common/string-max-len? 255))
(s/def ::booktitle (partial common/string-max-len? 255))
(s/def ::school (partial common/string-max-len? 255))
(s/def ::institution (partial common/string-max-len? 255))
(s/def ::year int?)
(s/def ::author (partial common/string-max-len? 255))
(s/def ::title (partial common/string-max-len? 255))
(s/def ::note (partial common/string-max-len? 1000))

;; Certain sub-types of source (e.g., book, or article) require non-empty  keys.
(s/def :not-empty/author (s/and ::author not-empty))
(s/def :not-empty/editor (s/and ::editor not-empty))
(s/def :not-empty/title (s/and ::title not-empty))
(s/def :not-empty/journal (s/and ::journal not-empty))
(s/def :not-empty/publisher (s/and ::publisher not-empty))
(s/def :not-empty/booktitle (s/and ::booktitle not-empty))
(s/def :not-empty/chapter (s/and ::chapter not-empty))
(s/def :not-empty/pages (s/and ::pages not-empty))
(s/def :not-empty/school (s/and ::school not-empty))
(s/def :not-empty/institution (s/and ::institution not-empty))
(s/def :not-empty/note (s/and ::note not-empty))

;; OLD-specific keys
(s/def ::file-id (s/nilable ::common/id))
(s/def ::file (s/nilable ::file/mini-file))
(s/def ::datetime-modified ::common/datetime-string)

;; Optional BibTeX keys
(s/def ::address (partial common/string-max-len? 1000))
(s/def ::annote string?)
(s/def ::edition (partial common/string-max-len? 255))
(s/def ::howpublished (partial common/string-max-len? 255))
(s/def ::key-field (partial common/string-max-len? 255))
(s/def ::month (partial common/string-max-len? 100))
(s/def ::number (partial common/string-max-len? 100))
(s/def ::organization (partial common/string-max-len? 255))
(s/def ::series (partial common/string-max-len? 255))
(s/def ::type-field (partial common/string-max-len? 255))
(s/def ::url (partial common/string-max-len? 1000))
(s/def ::volume (partial common/string-max-len? 100))

;; Optional Non-standard BibTeX keys
(s/def ::abstract (partial common/string-max-len? 1000))
(s/def ::affiliation (partial common/string-max-len? 255))
(s/def ::contents (partial common/string-max-len? 255))
(s/def ::copyright (partial common/string-max-len? 255))
(s/def ::keywords (partial common/string-max-len? 255))
(s/def ::language (partial common/string-max-len? 255))
(s/def ::location (partial common/string-max-len? 255))
(s/def ::size (partial common/string-max-len? 255))
(s/def ::price (partial common/string-max-len? 100))
(s/def ::mrnumber (partial common/string-max-len? 25))
(s/def ::isbn (partial common/string-max-len? 20))
(s/def ::issn (partial common/string-max-len? 20))
(s/def ::lccn (partial common/string-max-len? 20))

;; The ::mini-source is a read source. It is what we receive from the OLD when a
;; source is associated to another entity, e.g., a form or a collection. It has
;; a subset of the keys of a standard/regular read ::source.
(s/def ::mini-source (s/keys :req-un
                             [::id
                              ::crossref
                              ::type
                              ::key
                              ::journal
                              ::editor
                              ::chapter
                              ::pages
                              ::publisher
                              ::booktitle
                              ::school
                              ::institution
                              ::year
                              ::author
                              ::title
                              ::note]
                             :opt-un
                             [::crossref-source]))
(s/def ::mini-sources (s/coll-of ::mini-source))

;; TODO: there is a lot of repetition between ::mini-source, ::source and
;; ::write-source. We could consider being clever and using s/merge to DRY this
;; up.

(s/def ::source (s/keys :req-un
                        ;; core keys
                        [::id
                         ::crossref
                         ::crossref-source
                         ::crossref-source-id
                         ::type
                         ::key
                         ::journal
                         ::editor
                         ::chapter
                         ::pages
                         ::publisher
                         ::booktitle
                         ::school
                         ::institution
                         ::year
                         ::author
                         ::title
                         ::note
                         ;; OLD-specific keys
                         ::file-id
                         ::file
                         ::datetime-modified
                         ;; Optional keys
                         ::address
                         ::annote
                         ::edition
                         ::howpublished
                         ::key-field
                         ::month
                         ::number
                         ::organization
                         ::series
                         ::type-field
                         ::url
                         ::volume
                         ;; Non-standard keys
                         ::abstract
                         ::affiliation
                         ::contents
                         ::copyright
                         ::keywords
                         ::language
                         ::location
                         ::size
                         ::price
                         ::mrnumber
                         ::isbn
                         ::issn
                         ::lccn]))

;; Write Source
;;
;; The spec ::write-source defines what a new Source must look like when making
;; a POST request to an OLD's /forms endpoint.
;;
;; WARNING: validation via crossref is NOT implemented. Basically, BibTeX entry
;; types (and therefore OLD sources) can satisfy requirements on keys by falling
;; back to those values on a parent, said parent being referenced in the child's
;; :crossref key using the parent's :key key.

(s/def :write-source/file (s/nilable ::common/id))

(s/def ::write-source-common
  (s/keys :req-un [::type
                   ::key
                   :write-source/file
                   ::address
                   ::annote
                   ::author
                   ::booktitle
                   ::chapter
                   ::crossref
                   ::edition
                   ::editor
                   ::howpublished
                   ::institution
                   ::journal
                   ::key-field
                   ::month
                   ::note
                   ::number
                   ::organization
                   ::pages
                   ::publisher
                   ::school
                   ::series
                   ::title
                   ::type-field
                   ::url
                   ::volume
                   ::year
                   ;; non-standard
                   ::affiliation
                   ::abstract
                   ::contents
                   ::copyright
                   ::isbn
                   ::issn
                   ::lccn
                   ::keywords
                   ::language
                   ::location
                   ::mrnumber
                   ::price
                   ::size]))

;; Use a multi-spec to define write-source

(defmulti write-source :type)

(defmethod write-source "article" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/journal])))

(defmethod write-source "book" [_]
  (s/merge ::write-source-common
           (s/or :author-book (s/keys :req-un [:not-empty/author
                                               :not-empty/title
                                               :not-empty/publisher])
                 :editor-book (s/keys :req-un [:not-empty/editor
                                               :not-empty/title
                                               :not-empty/publisher]))))

(defmethod write-source "booklet" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/title])))

(defmethod write-source "manual" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/title])))

(defmethod write-source "proceedings" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/title])))

(defmethod write-source "conference" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/booktitle])))

(defmethod write-source "inproceedings" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/booktitle])))

(defmethod write-source "inbook" [_]
  (s/merge ::write-source-common
           (s/or :author-chapter-inbook (s/keys :req-un [:not-empty/author
                                                         :not-empty/title
                                                         :not-empty/chapter
                                                         :not-empty/publisher])
                 :editor-chapter-inbook (s/keys :req-un [:not-empty/editor
                                                         :not-empty/title
                                                         :not-empty/chapter
                                                         :not-empty/publisher])
                 :author-pages-inbook (s/keys :req-un [:not-empty/author
                                                       :not-empty/title
                                                       :not-empty/pages
                                                       :not-empty/publisher])
                 :editor-pages-inbook (s/keys :req-un [:not-empty/editor
                                                       :not-empty/title
                                                       :not-empty/pages
                                                       :not-empty/publisher]))))

(defmethod write-source "incollection" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/booktitle
                            :not-empty/publisher])))

(defmethod write-source "phdthesis" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/school])))

(defmethod write-source "mastersthesis" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/school])))

(defmethod write-source "techreport" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/institution])))

(defmethod write-source "unpublished" [_]
  (s/merge ::write-source-common
           (s/keys :req-un [:not-empty/author
                            :not-empty/title
                            :not-empty/note])))

(defmethod write-source "misc" [_] ::write-source-common)

(s/def ::write-source (s/multi-spec write-source ::type))

(defn write-source-valid? [write-source]
  (s/valid? ::write-source write-source))

(defn write-source-explain-data [write-source]
  (s/explain-data ::write-source write-source))
