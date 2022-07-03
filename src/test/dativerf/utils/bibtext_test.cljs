(ns test.dativerf.utils.bibtext-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.utils.bibtex :as sut]))

(t/deftest parse-bibtex-single-name-works
  (t/testing "parseable inputs are parsed correctly"
    (doseq [[k v]
            {"John Paul Jones"
             {:first ["John" "Paul"] :last ["Jones"] :von [] :jr []}
             "Jones, John Paul"
             {:first ["John" "Paul"] :last ["Jones"] :von [] :jr []}
             "Brinch Hansen, Per"
             {:first ["Per"] :last ["Brinch" "Hansen"] :von [] :jr []}
             "Ludwig von Beethoven"
             {:first ["Ludwig"] :last ["Beethoven"] :von ["von"] :jr []}
             "von Beethoven, Ludwig"
             {:first ["Ludwig"] :last ["Beethoven"] :von ["von"] :jr []}
             "{von Beethoven}, Ludwig" ;; This is bad BibTeX, but the parse is expected
             {:first ["Ludwig"] :last ["von Beethoven"] :von [] :jr []}
             "van der Wal, John Paul"
             {:first ["John" "Paul"] :last ["Wal"] :von ["van" "der"] :jr []}
             "Charles Louis Xavier Joseph de la Vallee Poussin"
             {:first ["Charles" "Louis" "Xavier" "Joseph"] :last ["Vallee" "Poussin"]
              :von ["de" "la"] :jr []}
             "Donald~E. Knuth"
             {:first ["Donald~E."] :last ["Knuth"] :von [] :jr []}
             "{Barnes and Noble, Inc.}"
             {:first ["Barnes and Noble, Inc."] :last [] :von [] :jr []}
             "Ford, Jr., Henry"
             {:first ["Henry"] :last ["Ford"] :von [] :jr ["Jr."]}
             "{Steele Jr.}, Guy L."
             {:first ["Guy" "L."] :last ["Steele Jr."] :von [] :jr []}
             "Guy L. {Steele Jr.}"
             {:first ["Guy" "L."] :last ["Steele Jr."] :von [] :jr []}
             "von Beethoven, Jr., Ludwig"
             {:first ["Ludwig"] :last ["Beethoven"] :von ["von"] :jr ["Jr."]}}]
      (t/is (= v (#'sut/parse-bibtex-single-name k)))))
  (t/testing "unparseable inputs throw exceptions as expected"
    (t/is
     (= :unparseable
        (try (#'sut/parse-bibtex-single-name "A, B, C, D")
             (catch js/Error e (:error (ex-data e))))))))

(t/deftest name-tokenizer-works
  (doseq [[input expected]
          {"John Paul Jones" ["John" "Paul" "Jones"]
           "Jones, John Paul" ["Jones" "," "John" "Paul"]
           "Ludwig von Beethoven" ["Ludwig" "von" "Beethoven"]
           "von Beethoven, Ludwig" ["von" "Beethoven" "," "Ludwig"]
           "{Ludwig von Beethoven}" ["{Ludwig von Beethoven}"]}]
    (t/is (= expected (re-seq sut/tokenizer-pattern input)))))

(t/deftest parse-bibtex-name-works
  (t/is (= (#'sut/parse-bibtex-name "Ralph Alpher and Bethe, Hans and George Gamow")
           [{:first ["Ralph"] :last ["Alpher"] :von [] :jr []}
            {:first ["Hans"] :last ["Bethe"] :von [] :jr []}
            {:first ["George"] :last ["Gamow"] :von [] :jr []}])))

(t/deftest name-in-citation-form-works
  (doseq [[input expected]
          {"von Beethoven, Jr., Ludwig" "Beethoven"
           "A, B, C, D" "A, B, C, D"
           "Ralph Alpher and Bethe, Hans and George Gamow"
           "Alpher, Bethe and Gamow"}]
    (t/is (= expected (sut/name-in-citation-form input)))))
