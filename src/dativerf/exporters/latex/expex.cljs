(ns dativerf.exporters.latex.expex
  "ExPex is a particular library for formatting linguistic IGT examples in
  LaTeX.

  Rules
  - there is one A gloss: gla. We favour the transcription value, but use the
    first in a ranked list. The gla is always the only line that contains the
    grammaticality, as a prefix.
  - the rest of the transcription-type fields are B glosses: glb.
  - the translation line is a free translation: glft. Multiple translations
    should be separated by linebreaks \\.
  - Special formatting seems to be required for (some) grammaticality values
    (or just certain characters) in: the glft the context: for example, * has
    to be $\\ast{}$."
  (:require [dativerf.exporters.latex.utils :as latex-u]
            [dativerf.models.source :as source]
            [dativerf.models.speaker :as speaker]
            [dativerf.utils :as utils]
            [cljs-time.core :as time]
            [clojure.string :as str]))

(defn trailing-citation
  "ExPex allows a trailing citation which comes on the same line as the last
   free translation. We use this to cite the form, preferring speaker and date
   elicited, falling back to citation of a source. The OLD id is always
   included."
  [{:keys [id date-elicited source speaker]}]
  (str "\\trailingcitation{("
       (cond speaker
             (str (speaker/initials speaker)
                  (when date-elicited
                    (str ", " (utils/date->human-string date-elicited)))
                  ", OLD ID: "
                  id)
             source
             (str (source/citation source) ", OLD ID: " id)
             :else
             (str "OLD ID: " id))
       ")}"))

(defn- expex-translations [form]
  (->> form
       :translations
       (filter (fn [v] (some-> v :transcription str/trim not-empty)))
       (map (fn [{:keys [transcription grammaticality]}]
              (if (not-empty grammaticality)
                (str (latex-u/math-asterisk (latex-u/escape-latex grammaticality)) "~`"
                     (latex-u/escape-latex transcription) "'")
                (str "`" (latex-u/escape-latex transcription) "'"))))
       (str/join "\\\\\n    ")))

(defn- glbs [glb-keys form]
  (->> glb-keys
       (map (fn [k] (-> form k str/trim latex-u/escape-latex)))
       (str/join "//\n    \\glb ")))

(defn- ex [body] (str "\\ex\n" body "\\xe\n"))
(defn- gl [body] (str "  \\begingl\n" body "  \\endgl\n"))
(defn- gla [body] (str "    \\gla " body "//\n"))
(defn- glb [body] (str "    \\glb " body "//\n"))
(defn- glft [body] (str "    \\glft " body "//\n"))

(defn export [form]
  (let [transcription-keys [:transcription
                            :narrow-phonetic-transcription
                            :phonetic-transcription
                            :morpheme-break]
        gla-key (->> transcription-keys
                     (filter (fn [k] (some-> form k str/trim not-empty)))
                     first)
        glb-keys (->> (conj transcription-keys :morpheme-gloss)
                      (filter (fn [k] (and (not= gla-key k)
                                           (some-> form k str/trim not-empty)))))]
    (ex
     (str
      (gl
       (str
        (gla (str (-> form :grammaticality str/trim latex-u/escape-latex)
                  (-> form gla-key str/trim latex-u/escape-latex)))
        (when (seq glb-keys) (glb (glbs glb-keys form)))
        (glft (str (expex-translations form) (trailing-citation form)))))
      (latex-u/form-medatadata-itemization 2 form)))))

(defn expex-article [title author date body]
  (str
   latex-u/xelatex-preamble
   "\\usepackage{expex}\n"
   "\\begin{document}\n"
   "\\title{" title "}\n"
   "\\author{" author "}\n"
   "\\date{" date "}\n"
   "\\maketitle\n"
   "\n"
   body
   "\n"
   "\\end{document}"))

(defn export-forms [forms {:keys [user]}]
  (expex-article
   "DativeRF Expex LaTeX Export"
   (if user
     (str (:first-name user) " " (:last-name user))
     "Unknown Author")
   (utils/date->human-string (time/now))
   (str/join "\n" (map export forms))))
