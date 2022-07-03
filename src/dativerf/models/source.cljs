(ns dativerf.models.source
  "Contains constructors for source maps, machinery for accessing source keys
  (which is non-standard), and helpers to get citation-related strings."
  (:require [clojure.string :as str]
            [dativerf.specs.source :as source-spec]
            [dativerf.utils.bibtex :as bibtex]))

(def default-write-source
  {:type ""
   :key ""
   :file nil
   :address ""
   :annote ""
   :author ""
   :booktitle ""
   :chapter ""
   :crossref "" ;; must be key of an existing source, but only OLD can validate that
   :edition ""
   :editor ""
   :howpublished ""
   :institution ""
   :journal ""
   :key-field ""
   :month ""
   :note ""
   :number ""
   :organization ""
   :pages ""
   :publisher ""
   :school ""
   :series ""
   :title ""
   :type-field ""
   :url ""
   :volume ""
   :year 0
   :affiliation ""
   :abstract ""
   :contents ""
   :copyright ""
   :isbn ""
   :issn ""
   :lccn ""
   :keywords ""
   :language ""
   :location ""
   :mrnumber ""
   :price ""
   :size ""})

;; Currently unused, this might be useful for the GUI for source
;; creation/editing.
(def bibtex-entry-types
  {:article [:author :title :journal :year]
   :book [[:author :editor] :title :publisher :year]
   :booklet [:title]
   :conference [:author :title :booktitle :year]
   :inbook [[:author :editor] :title [:chapter :pages] :publisher :year]
   :incollection [:author :title :booktitle :publisher :year]
   :inproceedings [:author :title :booktitle :year]
   :manual [:title]
   :mastersthesis [:author :title :school :year]
   :misc []
   :phdthesis [:author :title :school :year]
   :proceedings [:title :year]
   :techreport [:author :title :institution :year]
   :unpublished [:author :title :note]})

(defn article
  ([key author title journal year] (article key author title journal year {}))
  ([key author title journal year fields]
   (-> default-write-source
       (assoc :type "article"
              :key key
              :author author
              :title title
              :journal journal
              :year year)
       (merge fields))))

(defn book
  ([key author editor title publisher year]
   (book key author editor title publisher year {}))
  ([key author editor title publisher year fields]
   (-> default-write-source
       (assoc :type "book"
              :key key
              :author (or author "")
              :editor (or editor "")
              :title title
              :publisher publisher
              :year year)
       (merge fields))))

(defn booklet
  ([key title] (booklet key title {}))
  ([key title fields]
   (-> default-write-source
       (assoc :type "booklet"
              :key key
              :title title)
       (merge fields))))

(defn conference
  ([key author title booktitle year]
   (conference key author title booktitle year {}))
  ([key author title booktitle year fields]
   (-> default-write-source
     (assoc :type "conference"
            :key key
            :author author
            :title title
            :booktitle booktitle
            :year year)
     (merge fields))))

(defn inbook
  ([key author editor title chapter pages publisher year]
   (inbook key author editor title chapter pages publisher year {}))
  ([key author editor title chapter pages publisher year fields]
   (-> default-write-source
       (assoc :type "inbook"
              :key key
              :author (or author "")
              :editor (or editor "")
              :title title
              :chapter (or chapter "")
              :pages (or pages "")
              :publisher publisher
              :year year)
       (merge fields))))

;; TODO: complete these as per above, but I'm not sure it's worth the effort.
(defn incollection [key author title booktitle publisher year])
(defn inproceedings [key author title booktitle year])
(defn manual [key title])
(defn mastersthesis [key author title school year])
(defn misc [key])
(defn phdthesis [key author title school year])
(defn proceedings [key title year])
(defn techreport [key author title institution year])
(defn unpublished [key author title note])


;; Source getters (!)
;;
;; Sources need getters because they can merge with parent sources that they
;; carry as values. We there fore need special getters for sources (gross).
;; Alternatively, we could 'normalize' sources (i.e., pre-merge them) upon entry
;; into Dative. TODO: consider!

(defn- crossref-key
  "Get key from this source's crossref-source val. NOT recursive. (I'm not sure
  the OLD allows construction of crossref chains of length three or more.)"
  [source key]
  (some-> source :crossref-source key))

(defn s-get [source key] (or (get source key) (crossref-key source key)))

(defn s-keys [source keys] (for [key keys] (s-get source key)))

(defn empty-string->nil [s] (some-> s str/trim not-empty))

(defn author-in-citation-form
  "Return an author in citation form (string) for the supplied source object.
  Falls back to the editor in citation form, else the title, else 'no author'."
  [{:as source}]
  (let [[author editor title] (s-keys source [:author :editor :title])
        [author editor title] (map empty-string->nil [author editor title])]
    (cond author (bibtex/name-in-citation-form author)
          editor (bibtex/name-in-citation-form editor)
          title title
          :else "no author")))

(defn year [source] (or (s-get source :year) -3000))

(defn citation [source]
  (str (author-in-citation-form source) " (" (year source) ")"))
