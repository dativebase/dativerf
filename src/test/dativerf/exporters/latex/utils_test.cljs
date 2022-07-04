(ns test.dativerf.exporters.latex.utils-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.utils :as utils]
            [dativerf.exporters.latex.utils :as sut]))

(t/deftest form-medatadata-itemization-works
  (t/is (= (str "\\begin{itemize}\n"
                "  \\item OLD ID: 1\n"
                "\\end{itemize}\n")
           (sut/form-medatadata-itemization {:id 1})))
  (t/is (= (str "\\begin{itemize}\n"
                "  \\item Comments: some comments\n"
                "  \\item OLD ID: 1\n"
                "\\end{itemize}\n")
           (sut/form-medatadata-itemization {:comments "some comments" :id 1})))
  (t/is (= (str "    \\begin{itemize}\n"
                "      \\item Comments: some comments\n"
                "      \\item Speaker comments: speaker comments\n"
                "      \\item OLD ID: 1\n"
                "    \\end{itemize}\n")
           (sut/form-medatadata-itemization 4 {:comments "some comments"
                                               :speaker-comments "speaker comments"
                                               :id 1})))
  (t/is (= (str "    \\begin{itemize}\n"
                "      \\item Comments: some comments\n"
                "      \\item Speaker comments: speaker comments\n"
                "      \\item Speaker: BM\n"
                "      \\item Date elicited: Fri, 28 Oct 2022\n"
                "      \\item OLD ID: 1\n"
                "    \\end{itemize}\n")
           (sut/form-medatadata-itemization 4 {:comments "some comments"
                                               :speaker-comments "speaker comments"
                                               :speaker {:first-name "Beth"
                                                         :last-name "Marcus"}
                                               :date-elicited
                                               (utils/parse-date-string "2022-10-28")
                                               :id 1})))
  (t/is (= (str "    \\begin{itemize}\n"
                "      \\item Comments: some comments\n"
                "      \\item Speaker comments: speaker comments\n"
                "      \\item Speaker: BM\n"
                "      \\item Source: Avery (2018)\n"
                "      \\item OLD ID: 1\n"
                "    \\end{itemize}\n")
           (sut/form-medatadata-itemization 4 {:comments "some comments"
                                               :speaker-comments "speaker comments"
                                               :speaker {:first-name "Beth"
                                                         :last-name "Marcus"}
                                               :source {:author "Anne Avery"
                                                        :year 2018}
                                               :id 1}))))
