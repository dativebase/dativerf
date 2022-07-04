(ns test.dativerf.exporters.latex.expex-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.exporters.latex.expex :as sut]
            [dativerf.utils :as utils]))

(t/deftest expex-form-export-works
  (t/is
   (= (str
       "\\ex\n"
       "  \\begingl\n"
       "    \\gla *les chiens sont lala//\n"
       "    \\glb le-s chien-s sont la-la//\n"
       "    \\glb DET-PL dog-PL be.3PL DIST.DEM-DIST.DEM//\n"
       "    \\glft `the dogs are way over there'\\\\\n"
       "    $\\ast{}$~`the wolves are way over there'\\trailingcitation{(AB, Fri, 28 Oct 2022, OLD ID: 2)}//\n"
       "  \\endgl\n"
       "  \\begin{itemize}\n"
       "    \\item Comments: Alea iacta est\n"
       "    \\item Speaker comments: Indeed\n"
       "    \\item Speaker: AB\n"
       "    \\item Date elicited: Fri, 28 Oct 2022\n"
       "    \\item Source: Beethoven (1815)\n"
       "    \\item OLD ID: 2\n"
       "  \\end{itemize}\n"
       "\\xe\n")
      (sut/export {:id 2
                   :transcription "les chiens sont lala"
                   :grammaticality "*"
                   :morpheme-break "le-s chien-s sont la-la"
                   :morpheme-gloss "DET-PL dog-PL be.3PL DIST.DEM-DIST.DEM"
                   :translations
                   [{:transcription "the dogs are way over there" :grammaticality ""}
                    {:transcription "the wolves are way over there" :grammaticality "*"}]
                   :comments "Alea iacta est"
                   :speaker-comments "Indeed"
                   :speaker {:first-name "Anne" :last-name "Barclay"}
                   :date-elicited (utils/parse-date-string "2022-10-28")
                   :source {:author "von Beethoven, Jr., Ludwig"
                            :year 1815}}))))

(t/deftest trailing-citation-works
  (t/is (= "\\trailingcitation{(Dunham (2022), OLD ID: 2)}"
           (sut/trailing-citation
            {:id 2
             :source {:author "Dunham, Joel" :year 2022}})))
  (t/is (= "\\trailingcitation{(JD, Wed, 23 Feb 2022, OLD ID: 2)}"
           (sut/trailing-citation
            {:id 2
             :date-elicited (utils/parse-date-string "2022-02-23")
             :speaker {:first-name "Joel" :last-name "Dunham"}}))))
