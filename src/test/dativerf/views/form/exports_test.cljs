(ns test.dativerf.views.form.exports-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.views.form.exports :as sut]))

(t/deftest leipzig-igt-export-works
  (doseq [[form expected]
          (->> [{:grammaticality "*"
                 :transcription "chiense"
                 :morpheme-break "chien-s"
                 :morpheme-gloss "dog-PL"
                 :translations
                 [{:grammaticality ""
                   :transcription "dog"}
                  {:grammaticality "?"
                   :transcription "wolf"}]}
                (str "<div data-gloss>\n"
                     "  <p>*chiense</p>\n"
                     "  <p>chien-s</p>\n"
                     "  <p>dog-PL</p>\n"
                     "  <p>'dog'</p>\n"
                     "  <p>'?wolf'</p>\n"
                     "</div>")
                {:grammaticality ""
                 :transcription "chiens"
                 :morpheme-break "chien-s"
                 :morpheme-gloss "dog-PL"
                 :translations
                 [{:grammaticality ""
                   :transcription "dog"}]}
                (str "<div data-gloss>\n"
                     "  <p>chiens</p>\n"
                     "  <p>chien-s</p>\n"
                     "  <p>dog-PL</p>\n"
                     "  <p>'dog'</p>\n"
                     "</div>")
                {}
                (str "<div data-gloss>\n"
                     "  <p>no data</p>\n"
                     "</div>")]
               (partition 2))]
    (t/is (= expected (sut/leipzig-igt-export form)))))
