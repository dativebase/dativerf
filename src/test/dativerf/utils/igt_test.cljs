(ns test.dativerf.utils.igt-test
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require [cljs.test :as t :include-macros true]
            [clojure.data :as data]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [dativerf.specs.form :as form-spec]
            [dativerf.utils.igt :as sut]))

(def test-form-1
  {:grammaticality "*"
   :transcription "Está situada no sul da Região Nordeste, fazendo limite com outros oito estados brasileiros - é o estado brasileiro que mais faz divisas."
   :morpheme-break "Est-á situa-da em-o sul de-a Região Nord-este faz-endo limite com outr-os oito estad-os brasil-eir-os - é o estado brasil-eir-o que mais faz divisa-s"
   :morpheme-gloss "be-3SG.PRS situate-PP in-DET.M.SG south of-DET.F.SG region north-east make-GER border with other-M.PL eight state-M.PL brazil-LOC-M.PL - be.3SG.PRS DET.M.SG state brazil-LOC-M.SG COMP more do.3SG.PRS border-PL"})

(def test-form-2
  {:transcription "Les chiens qui étaient vraiment fatigués et qui avaient mangé tous leur dîners étaient par terre à l'extérieur en train de dormir tranquillement."
   :morpheme-break "le-s chien-s qui ét-ai-ent vraiment fatigu-é-s et qui av-ai-ent mang-é tous leur dîner-s ét-ai-ent par terre à l=extérieur en train de dormir tranquille-ment"
   :morpheme-gloss "DET-PL dog-PL REL.PRO be-IMPF-3PL really tire-PP-PL and REL.PRO have-IMPF-3PL eat-3SG.PRS all POSS.3PL dinner-PL be-IMPF-3PL LOC land at DET=outside in the.course.of of sleep.INF calm-ADVZ"
   :grammaticality ""
   :translations
   [{:transcripton "The dogs that were really tired and had eaten all their dinners were on the ground outside sleeping quietly."
     :grammaticality ""}]})

(def test-form-1-igt-data
  '({:key :transcription,
     :indent 0,
     :row 0,
     :words
     [{:length 10, :word "*Está", :index 0}
      {:length 10, :word "situada", :index 1}
      {:length 11, :word "no", :index 2}
      {:length 5, :word "sul", :index 3}]}
    {:key :morpheme-break,
     :indent 0,
     :row 0,
     :words
     [{:length 10, :word "/Est-á", :index 0}
      {:length 10, :word "situa-da", :index 1}
      {:length 11, :word "em-o", :index 2}
      {:length 5, :word "sul", :index 3}]}
    {:key :morpheme-gloss,
     :indent 0,
     :row 0,
     :words
     [{:length 10, :word "be-3SG.PRS", :index 0}
      {:length 10, :word "situate-PP", :index 1}
      {:length 11, :word "in-DET.M.SG", :index 2}
      {:length 5, :word "south", :index 3}]}
    {:key :transcription,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "da", :index 4}
      {:length 6, :word "Região", :index 5}
      {:length 10, :word "Nordeste,", :index 6}
      {:length 8, :word "fazendo", :index 7}]}
    {:key :morpheme-break,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "de-a", :index 4}
      {:length 6, :word "Região", :index 5}
      {:length 10, :word "Nord-este", :index 6}
      {:length 8, :word "faz-endo", :index 7}]}
    {:key :morpheme-gloss,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "of-DET.F.SG", :index 4}
      {:length 6, :word "region", :index 5}
      {:length 10, :word "north-east", :index 6}
      {:length 8, :word "make-GER", :index 7}]}
    {:key :transcription,
     :indent 4,
     :row 2,
     :words
     [{:length 6, :word "limite", :index 8}
      {:length 4, :word "com", :index 9}
      {:length 10, :word "outros", :index 10}
      {:length 5, :word "oito", :index 11}
      {:length 10, :word "estados", :index 12}]}
    {:key :morpheme-break,
     :indent 4,
     :row 2,
     :words
     [{:length 6, :word "limite", :index 8}
      {:length 4, :word "com", :index 9}
      {:length 10, :word "outr-os", :index 10}
      {:length 5, :word "oito", :index 11}
      {:length 10, :word "estad-os", :index 12}]}
    {:key :morpheme-gloss,
     :indent 4,
     :row 2,
     :words
     [{:length 6, :word "border", :index 8}
      {:length 4, :word "with", :index 9}
      {:length 10, :word "other-M.PL", :index 10}
      {:length 5, :word "eight", :index 11}
      {:length 10, :word "state-M.PL", :index 12}]}
    {:key :transcription,
     :indent 6,
     :row 3,
     :words
     [{:length 15, :word "brasileiros", :index 13}
      {:length 1, :word "-", :index 14}
      {:length 10, :word "é", :index 15}
      {:length 8, :word "o", :index 16}]}
    {:key :morpheme-break,
     :indent 6,
     :row 3,
     :words
     [{:length 15, :word "brasil-eir-os", :index 13}
      {:length 1, :word "-", :index 14}
      {:length 10, :word "é", :index 15}
      {:length 8, :word "o", :index 16}]}
    {:key :morpheme-gloss,
     :indent 6,
     :row 3,
     :words
     [{:length 15, :word "brazil-LOC-M.PL", :index 13}
      {:length 1, :word "-", :index 14}
      {:length 10, :word "be.3SG.PRS", :index 15}
      {:length 8, :word "DET.M.SG", :index 16}]}
    {:key :transcription,
     :indent 8,
     :row 4,
     :words
     [{:length 6, :word "estado", :index 17}
      {:length 15, :word "brasileiro", :index 18}
      {:length 4, :word "que", :index 19}
      {:length 4, :word "mais", :index 20}]}
    {:key :morpheme-break,
     :indent 8,
     :row 4,
     :words
     [{:length 6, :word "estado", :index 17}
      {:length 15, :word "brasil-eir-o", :index 18}
      {:length 4, :word "que", :index 19}
      {:length 4, :word "mais", :index 20}]}
    {:key :morpheme-gloss,
     :indent 8,
     :row 4,
     :words
     [{:length 6, :word "state", :index 17}
      {:length 15, :word "brazil-LOC-M.SG", :index 18}
      {:length 4, :word "COMP", :index 19}
      {:length 4, :word "more", :index 20}]}
    {:key :transcription,
     :indent 10,
     :row 5,
     :words
     [{:length 10, :word "faz", :index 21}
      {:length 9, :word "divisas.", :index 22}]}
    {:key :morpheme-break,
     :indent 10,
     :row 5,
     :words
     [{:length 10, :word "faz", :index 21}
      {:length 9, :word "divisa-s/", :index 22}]}
    {:key :morpheme-gloss,
     :indent 10,
     :row 5,
     :words
     [{:length 10, :word "do.3SG.PRS", :index 21}
      {:length 9, :word "border-PL", :index 22}]}))

(def test-form-2-igt-data
  '({:key :transcription,
     :indent 0,
     :row 0,
     :words
     [{:length 6, :word "Les", :index 0}
      {:length 7, :word "chiens", :index 1}
      {:length 7, :word "qui", :index 2}
      {:length 11, :word "étaient", :index 3}
      {:length 8, :word "vraiment", :index 4}]}
    {:key :morpheme-break,
     :indent 0,
     :row 0,
     :words
     [{:length 6, :word "/le-s", :index 0}
      {:length 7, :word "chien-s", :index 1}
      {:length 7, :word "qui", :index 2}
      {:length 11, :word "ét-ai-ent", :index 3}
      {:length 8, :word "vraiment", :index 4}]}
    {:key :morpheme-gloss,
     :indent 0,
     :row 0,
     :words
     [{:length 6, :word "DET-PL", :index 0}
      {:length 7, :word "dog-PL", :index 1}
      {:length 7, :word "REL.PRO", :index 2}
      {:length 11, :word "be-IMPF-3PL", :index 3}
      {:length 8, :word "really", :index 4}]}
    {:key :transcription,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "fatigués", :index 5}
      {:length 3, :word "et", :index 6}
      {:length 7, :word "qui", :index 7}
      {:length 13, :word "avaient", :index 8}]}
    {:key :morpheme-break,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "fatigu-é-s", :index 5}
      {:length 3, :word "et", :index 6}
      {:length 7, :word "qui", :index 7}
      {:length 13, :word "av-ai-ent", :index 8}]}
    {:key :morpheme-gloss,
     :indent 2,
     :row 1,
     :words
     [{:length 11, :word "tire-PP-PL", :index 5}
      {:length 3, :word "and", :index 6}
      {:length 7, :word "REL.PRO", :index 7}
      {:length 13, :word "have-IMPF-3PL", :index 8}]}
    {:key :transcription,
     :indent 4,
     :row 2,
     :words
     [{:length 11, :word "mangé", :index 9}
      {:length 4, :word "tous", :index 10}
      {:length 8, :word "leur", :index 11}
      {:length 9, :word "dîners", :index 12}]}
    {:key :morpheme-break,
     :indent 4,
     :row 2,
     :words
     [{:length 11, :word "mang-é", :index 9}
      {:length 4, :word "tous", :index 10}
      {:length 8, :word "leur", :index 11}
      {:length 9, :word "dîner-s", :index 12}]}
    {:key :morpheme-gloss,
     :indent 4,
     :row 2,
     :words
     [{:length 11, :word "eat-3SG.PRS", :index 9}
      {:length 4, :word "all", :index 10}
      {:length 8, :word "POSS.3PL", :index 11}
      {:length 9, :word "dinner-PL", :index 12}]}
    {:key :transcription,
     :indent 6,
     :row 3,
     :words
     [{:length 11, :word "étaient", :index 13}
      {:length 3, :word "par", :index 14}
      {:length 5, :word "terre", :index 15}
      {:length 2, :word "à", :index 16}
      {:length 11, :word "l'extérieur", :index 17}
      {:length 2, :word "en", :index 18}]}
    {:key :morpheme-break,
     :indent 6,
     :row 3,
     :words
     [{:length 11, :word "ét-ai-ent", :index 13}
      {:length 3, :word "par", :index 14}
      {:length 5, :word "terre", :index 15}
      {:length 2, :word "à", :index 16}
      {:length 11, :word "l=extérieur", :index 17}
      {:length 2, :word "en", :index 18}]}
    {:key :morpheme-gloss,
     :indent 6,
     :row 3,
     :words
     [{:length 11, :word "be-IMPF-3PL", :index 13}
      {:length 3, :word "LOC", :index 14}
      {:length 5, :word "land", :index 15}
      {:length 2, :word "at", :index 16}
      {:length 11, :word "DET=outside", :index 17}
      {:length 2, :word "in", :index 18}]}
    {:key :transcription,
     :indent 8,
     :row 4,
     :words
     [{:length 13, :word "train", :index 19}
      {:length 2, :word "de", :index 20}
      {:length 9, :word "dormir", :index 21}]}
    {:key :morpheme-break,
     :indent 8,
     :row 4,
     :words
     [{:length 13, :word "train", :index 19}
      {:length 2, :word "de", :index 20}
      {:length 9, :word "dormir", :index 21}]}
    {:key :morpheme-gloss,
     :indent 8,
     :row 4,
     :words
     [{:length 13, :word "the.course.of", :index 19}
      {:length 2, :word "of", :index 20}
      {:length 9, :word "sleep.INF", :index 21}]}
    {:key :transcription,
     :indent 10,
     :row 5,
     :words [{:length 16, :word "tranquillement.", :index 22}]}
    {:key :morpheme-break,
     :indent 10,
     :row 5,
     :words [{:length 16, :word "tranquille-ment/", :index 22}]}
    {:key :morpheme-gloss,
     :indent 10,
     :row 5,
     :words [{:length 16, :word "calm-ADVZ", :index 22}]}))

(t/deftest igt-data-works
  (t/testing "IGT data creation works as expected"
    (t/is (= test-form-1-igt-data (sut/igt-data test-form-1)))
    (t/is (= test-form-2-igt-data (sut/igt-data test-form-2)))
    (t/is (= '({:key :transcription
                :indent 0
                :row 0
                :words [{:length 3 :word "the" :index 0}]})
             (sut/igt-data {:transcription "the"})))
    (t/is (= '({:key :transcription
                :indent 0
                :row 0
                :words [{:length 9 :word "*walken" :index 0}]}
               {:key :morpheme-break
                :indent 0
                :row 0
                :words [{:length 9 :word "/walk-en/" :index 0}]}
               {:key :morpheme-gloss
                :indent 0
                :row 0
                :words [{:length 9 :word "marche-PP" :index 0}]})
             (sut/igt-data {:grammaticality "*"
                            :transcription "walken"
                            :morpheme-break "walk-en"
                            :morpheme-gloss "marche-PP"}))))
  (t/testing "IGT data configuration works"
    (let [form {:morpheme-break "le-s chien-s"
                :morpheme-gloss "DET-PL dog-PL"}]
      (t/is (= 4 (count (sut/igt-data form {:max-row-length 8}))))
      (t/is (= 2 (count (sut/igt-data form {:max-row-length 80})))))))

(defspec igt-data-works-for-all-valid-forms
  (prop/for-all [form (s/gen ::form-spec/igt-form)]
                (sut/valid-igt-data? (sut/igt-data form))))
