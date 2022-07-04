(ns dativerf.exporters.latex.utils
  "Utilities for LaTeX exporters."
  (:require [clojure.string :as str]
            [dativerf.models.speaker :as speaker]
            [dativerf.models.source :as source]
            [dativerf.utils :as utils]))

;; Dealing with the LaTeX implementation

(defn escape-latex
  "Escape the 10 LaTeX special characters."
  [string]
  (-> string
      (str/replace #"\\" "\\textbackslash")
      (str/replace #"\{" "\\{")
      (str/replace #"}" "\\}")
      (str/replace #"textbackslash" "textbackslash{}")
      (str/replace #"&" "\\&")
      (str/replace #"\$" "\\$")
      (str/replace #"#" "\\#")
      (str/replace #"_" "\\_")
      (str/replace #"~" "\\textasciitilde{}")
      (str/replace #"\^" "\\textasciicircum{}")))

(defn math-asterisk [string]
  (str/replace string #"\*" "$\\ast{}$"))

;; Reusable functionality for generating LaTeX stuff

(defn indent [n] (apply str (repeat n " ")))

(defn itemize
  ([items] (itemize 0 items))
  ([indentation items]
   (apply str
          (concat [(str (indent indentation) "\\begin{itemize}\n")]
                  (for [item items] (str (indent indentation) item))
                  [(str (indent indentation) "\\end{itemize}\n")]))))

(defn item [item-body] (str "  \\item " item-body "\n"))

(defn- comments [form]
  (when-let [comments (utils/get-empty-string->nil form :comments)]
    (str "Comments: " comments)))

(defn- speaker-comments [form]
  (when-let [speaker-comments (utils/get-empty-string->nil form :speaker-comments)]
    (str "Speaker comments: " speaker-comments)))

(defn- speaker-item-body [{:keys [speaker]}]
  (when speaker (str "Speaker: " (speaker/initials speaker))))

(defn- date-elicited-item-body [{:keys [date-elicited]}]
  (when date-elicited (str "Date elicited: "
                           (utils/date->human-string date-elicited))))

(defn- source-citation [{:keys [source]}]
  (and source (str "Source: "(source/citation source))))

(defn- old-id [{:keys [id]}] (str "OLD ID: " id))

(defn form-medatadata-itemization
  "Return a latex itemization (\\begin{itemize}\\item a\\end{itemize}) for the
  'core' metada of a form. This is somewhat arbitrary, but it's the stuff we'd
  expect users to want to list after an IGT representation of a form, say in an
  article. We return all of the following that are non-empty:
  - comments
  - speaker comments
  - speaker initials and date elicited
  - source citation
  - OLD form ID"
  ([form] (form-medatadata-itemization 0 form))
  ([indentation form]
   (or (some->> [(some-> form comments item)
                 (some-> form speaker-comments item)
                 (some-> form speaker-item-body item)
                 (some-> form date-elicited-item-body item)
                 (some-> form source-citation item)
                 (some-> form old-id item)]
                (filter some?)
                seq
                (itemize indentation))
       "")))

(def xelatex-preamble
  (str
   "\\documentclass[a4paper,english]{article}\n"
   "\\usepackage{fixltx2e}\n"
   "\\usepackage{cmap}\n"
   "\\usepackage[T1]{fontenc}\n"
   "\\usepackage{ifthen}\n"
   "\\usepackage{babel}\n"
   "\\usepackage{mathptmx}\n"
   "\\usepackage[scaled=.90]{helvet}\n"
   "\\usepackage{courier}\n"
   "\\usepackage{fontspec}\n"
   "\\defaultfontfeatures{Mapping=tex-text}\n"
   "\\usepackage{xunicode}\n"
   "\\usepackage{xltxtra}\n"
   "\\setmainfont{Charis SIL}\n"))
