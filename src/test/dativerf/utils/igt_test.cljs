(ns test.dativerf.utils.igt-test
  (:require [cljs.test :as t :include-macros true]
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
  '({:key :transcription
     :indent 0
     :row 0
     :words
     [{:length 10 :word "*Está"}
      {:length 10 :word "situada"}
      {:length 11 :word "no"}
      {:length 5 :word "sul"}
      {:length 11 :word "da"}
      {:length 6 :word "Região"}
      {:length 10 :word "Nordeste,"}
      {:length 8 :word "fazendo"}
      {:length 6 :word "limite"}]}
    {:key :morpheme-break
     :indent 0
     :row 0
     :words
     [{:length 10 :word "Est-á"}
      {:length 10 :word "situa-da"}
      {:length 11 :word "em-o"}
      {:length 5 :word "sul"}
      {:length 11 :word "de-a"}
      {:length 6 :word "Região"}
      {:length 10 :word "Nord-este"}
      {:length 8 :word "faz-endo"}
      {:length 6 :word "limite"}]}
    {:key :morpheme-gloss
     :indent 0
     :row 0
     :words
     [{:length 10 :word "be-3SG.PRS"}
      {:length 10 :word "situate-PP"}
      {:length 11 :word "in-DET.M.SG"}
      {:length 5 :word "south"}
      {:length 11 :word "of-DET.F.SG"}
      {:length 6 :word "region"}
      {:length 10 :word "north-east"}
      {:length 8 :word "make-GER"}
      {:length 6 :word "border"}]}
    {:key :transcription
     :indent 5
     :row 1
     :words
     [{:length 4 :word "com"}
      {:length 10 :word "outros"}
      {:length 5 :word "oito"}
      {:length 10 :word "estados"}
      {:length 15 :word "brasileiros"}
      {:length 1 :word "-"}
      {:length 10 :word "é"}
      {:length 8 :word "o"}
      {:length 6 :word "estado"}]}
    {:key :morpheme-break
     :indent 5
     :row 1
     :words
     [{:length 4 :word "com"}
      {:length 10 :word "outr-os"}
      {:length 5 :word "oito"}
      {:length 10 :word "estad-os"}
      {:length 15 :word "brasil-eir-os"}
      {:length 1 :word "-"}
      {:length 10 :word "é"}
      {:length 8 :word "o"}
      {:length 6 :word "estado"}]}
    {:key :morpheme-gloss
     :indent 5
     :row 1
     :words
     [{:length 4 :word "with"}
      {:length 10 :word "other-M.PL"}
      {:length 5 :word "eight"}
      {:length 10 :word "state-M.PL"}
      {:length 15 :word "brazil-LOC-M.PL"}
      {:length 1 :word "-"}
      {:length 10 :word "be.3SG.PRS"}
      {:length 8 :word "DET.M.SG"}
      {:length 6 :word "state"}]}
    {:key :transcription
     :indent 10
     :row 2
     :words
     [{:length 15 :word "brasileiro"}
      {:length 4 :word "que"}
      {:length 4 :word "mais"}
      {:length 10 :word "faz"}
      {:length 9 :word "divisas."}]}
    {:key :morpheme-break
     :indent 10
     :row 2
     :words
     [{:length 15 :word "brasil-eir-o"}
      {:length 4 :word "que"}
      {:length 4 :word "mais"}
      {:length 10 :word "faz"}
      {:length 9 :word "divisa-s"}]}
    {:key :morpheme-gloss
     :indent 10
     :row 2
     :words
     [{:length 15 :word "brazil-LOC-M.SG"}
      {:length 4 :word "COMP"}
      {:length 4 :word "more"}
      {:length 10 :word "do.3SG.PRS"}
      {:length 9 :word "border-PL"}]}))

(def test-form-2-igt-data
  '({:key :transcription
     :indent 0
     :row 0
     :words
     [{:length 6 :word "Les"}
      {:length 7 :word "chiens"}
      {:length 7 :word "qui"}
      {:length 11 :word "étaient"}
      {:length 8 :word "vraiment"}
      {:length 11 :word "fatigués"}
      {:length 3 :word "et"}
      {:length 7 :word "qui"}
      {:length 13 :word "avaient"}]}
    {:key :morpheme-break
     :indent 0
     :row 0
     :words
     [{:length 6 :word "le-s"}
      {:length 7 :word "chien-s"}
      {:length 7 :word "qui"}
      {:length 11 :word "ét-ai-ent"}
      {:length 8 :word "vraiment"}
      {:length 11 :word "fatigu-é-s"}
      {:length 3 :word "et"}
      {:length 7 :word "qui"}
      {:length 13 :word "av-ai-ent"}]}
    {:key :morpheme-gloss
     :indent 0
     :row 0
     :words
     [{:length 6 :word "DET-PL"}
      {:length 7 :word "dog-PL"}
      {:length 7 :word "REL.PRO"}
      {:length 11 :word "be-IMPF-3PL"}
      {:length 8 :word "really"}
      {:length 11 :word "tire-PP-PL"}
      {:length 3 :word "and"}
      {:length 7 :word "REL.PRO"}
      {:length 13 :word "have-IMPF-3PL"}]}
    {:key :transcription
     :indent 5
     :row 1
     :words
     [{:length 11 :word "mangé"}
      {:length 4 :word "tous"}
      {:length 8 :word "leur"}
      {:length 9 :word "dîners"}
      {:length 11 :word "étaient"}
      {:length 3 :word "par"}
      {:length 5 :word "terre"}
      {:length 2 :word "à"}
      {:length 11 :word "l'extérieur"}
      {:length 2 :word "en"}]}
    {:key :morpheme-break
     :indent 5
     :row 1
     :words
     [{:length 11 :word "mang-é"}
      {:length 4 :word "tous"}
      {:length 8 :word "leur"}
      {:length 9 :word "dîner-s"}
      {:length 11 :word "ét-ai-ent"}
      {:length 3 :word "par"}
      {:length 5 :word "terre"}
      {:length 2 :word "à"}
      {:length 11 :word "l=extérieur"}
      {:length 2 :word "en"}]}
    {:key :morpheme-gloss
     :indent 5
     :row 1
     :words
     [{:length 11 :word "eat-3SG.PRS"}
      {:length 4 :word "all"}
      {:length 8 :word "POSS.3PL"}
      {:length 9 :word "dinner-PL"}
      {:length 11 :word "be-IMPF-3PL"}
      {:length 3 :word "LOC"}
      {:length 5 :word "land"}
      {:length 2 :word "at"}
      {:length 11 :word "DET=outside"}
      {:length 2 :word "in"}]}
    {:key :transcription
     :indent 10
     :row 2
     :words
     [{:length 13 :word "train"}
      {:length 2 :word "de"}
      {:length 9 :word "dormir"}
      {:length 15 :word "tranquillement."}]}
    {:key :morpheme-break
     :indent 10
     :row 2
     :words
     [{:length 13 :word "train"}
      {:length 2 :word "de"}
      {:length 9 :word "dormir"}
      {:length 15 :word "tranquille-ment"}]}
    {:key :morpheme-gloss
     :indent 10
     :row 2
     :words
     [{:length 13 :word "the.course.of"}
      {:length 2 :word "of"}
      {:length 9 :word "sleep.INF"}
      {:length 15 :word "calm-ADVZ"}]}))

(t/deftest igt-data-works
  (t/testing "IGT data creation works as expected"
    (t/is (= test-form-1-igt-data (sut/igt-data test-form-1)))
    (t/is (= test-form-2-igt-data (sut/igt-data test-form-2)))
    (t/is (= '({:key :transcription
                :indent 0
                :row 0
                :words [{:length 3 :word "the"}]})
             (sut/igt-data {:transcription "the"})))
    (t/is (= '({:key :transcription
                :indent 0
                :row 0
                :words [{:length 9 :word "*walken"}]}
               {:key :morpheme-break
                :indent 0
                :row 0
                :words [{:length 9 :word "walk-en"}]}
               {:key :morpheme-gloss
                :indent 0
                :row 0
                :words [{:length 9 :word "marche-PP"}]})
             (sut/igt-data {:grammaticality "*"
                            :transcription "walken"
                            :morpheme-break "walk-en"
                            :morpheme-gloss "marche-PP"}))))
  (t/testing "IGT data configuration works"
    (let [form {:morpheme-break "le-s chien-s"
                :morpheme-gloss "DET-PL dog-PL"}]
      (t/is (= 4 (count (sut/igt-data form {:max-row-length 8}))))
      (t/is (= 2 (count (sut/igt-data form {:max-row-length 80})))))))
