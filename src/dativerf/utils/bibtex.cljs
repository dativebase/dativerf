(ns dativerf.utils.bibtex
  "Utils for handling BibTeX entities. These are related to OLD source entities,
  since sources are essentially BibTeX entries."
  (:require [clojure.string :as str]))

(defn- clean-token [token] (str/replace token #"^\{(.*)\}$" "$1"))

(def tokenizer-pattern
  (re-pattern
   (str
    ;; a brace-enclosed single token; a sequence of non-braces between braces
    "\\{[^\\{\\}]+\\}|"
    ;; a sequence of non-spaces and non-commas
    "[^ ,]+|"
    ;; comma
    ",")))

(defn- last-names
  "Given a parsed BibTeX name (an array of name maps), return a string of
  conjoined last names, e.g., 'Smith, Yang, and Moore'."
  [parsed-name]
  (let [lnames (map (fn [{:keys [last]}] (str/join " " last)) parsed-name)]
    (if (= 1 (count lnames))
      (first lnames)
      (str (str/join ", " (butlast lnames)) " and " (last lnames)))))

;; TODO: this will fail for "von" parts that contain non-initial capital letters.
;; However, a definition like the following has its own problems:
;; (defn- von? [token] (= (first token) (str/lower-case (first token))))
;; A regex may be needed.
(defn- von? [token] (= token (str/lower-case token)))

(defn- first-von-last [tokens]
  (reduce-kv
   (fn [agg idx token]
     (update agg
             (cond (zero? idx) :first
                   (= (dec (count tokens)) idx) :last
                   (von? token) :von
                   (seq (:von agg)) :last
                   :else :first)
             conj token))
   {:first [] :von [] :last [] :jr []}
   tokens))

(defn- von-last-first [tokens]
  (reduce
   (fn [agg token]
     (update agg
             (cond (= "," token) :comma
                   (seq (:comma agg)) :first
                   :else (if (von? token) :von :last))
             conj token))
   {:first [] :von [] :last [] :jr [] :comma []}
   tokens))

(defn- von-last-jr-first [tokens]
  (reduce
   (fn [agg token]
     (update agg
             (cond (= "," token) :comma
                   (empty? (:comma agg)) (if (von? token) :von :last)
                   (= 1 (count (:comma agg))) :jr
                   :else :first)
             conj token))
   {:first [] :von [] :last [] :jr [] :comma []}
   tokens))

(defn- parse-bibtex-single-name [name]
  (let [tokens (mapv clean-token (re-seq tokenizer-pattern name))]
    (select-keys
     ((case (count (for [t tokens :when (= "," t)] t))
        0 first-von-last
        1 von-last-first
        2 von-last-jr-first
        (throw (ex-info
                "Unparseable BibTeX name contains more than two comma tokens"
                {:name name :error :unparseable}))) tokens)
     [:first :last :von :jr])))

(defn- parse-bibtex-name
  "Parse a BibTeX name that may contain multiple names separated by ' and '.
  Return a vec of parsed name maps, each representing the parse of a BibTeX name.
  A name map has four keys whose values are each (possibly empty) vecs: :first
  :last :von and :jr. For example, the parse for 'von Beethoven, Jr., Ludwig' is
  {:first [Ludwig] :last [Beethoven] :von [von] :jr [Jr.]}. See
  http://nwalsh.com/tex/texhelp/bibtx-23.html. Note: this may not work exactly
  as BibTeX does it, but it should be good enough."
  [name] (mapv parse-bibtex-single-name (str/split name #" and ")))

(defn name-in-citation-form [name]
  (try (last-names (parse-bibtex-name name))
       (catch js/Error _ name)))

(defn author
  "Return an author for supplied source object. We return the author in
  citation form if there is an author; otherwise we return the editor in
  citation form or the title or 'no author'."
  [{:keys [author editor title]}]
  (let [prep (fn [x] (some-> x str/trim not-empty))
        [author editor title] (map prep [author editor title])]
    (cond author (name-in-citation-form author)
          editor (name-in-citation-form editor)
          title title
          :else "no author")))

;; TODO: it smells that year is int according to the spec but the not found
;; value is a string.
(defn year [source] (or (:year source) "no year"))
