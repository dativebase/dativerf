(ns test.dativerf.models.source-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.models.source :as sut]
            [dativerf.specs.source :as source-spec]))

;; TODO: This test should probably be moved into a specs.source-test ns.
(t/deftest source-spec-works
  (t/testing "Articles are validated"
    (let [good-article (sut/article "dunham2022" "Joel Dunham" "Great Article"
                                    "Some Journal" 2022)
          bad-article (sut/article "dunham2022" "" "Great Article"
                                   "Some Journal" 2022)]
      (t/is (true? (source-spec/write-source-valid? good-article)))
      (t/is (false? (source-spec/write-source-valid? bad-article)))
      ;; The problem is it's an article with an empty author:
      (t/is (= ["article" :author]
               (-> (source-spec/write-source-explain-data bad-article)
                   :cljs.spec.alpha/problems
                   first
                   :path)))))

  (t/testing "Books are validated"
    (let [good-book (sut/book "dunham2022" "Joel Dunham" "Editors & Sons"
                              "The Every Book" "Random House" 2022)
          no-editor-book (sut/book "dunham2022" "Joel Dunham" nil "The Every Book"
                                   "Random House" 2022)
          no-author-book (sut/book "dunham2022" nil "Editors & Sons"
                                   "The Every Book" "Random House" 2022)
          bad-book (sut/book "dunham2022" nil nil "The Every Book"
                             "Random House" 2022)]
      (t/is (true? (source-spec/write-source-valid? good-book)))
      (t/testing "Editor can be empty, if author isn't"
        (t/is (true? (source-spec/write-source-valid? no-editor-book))))
      (t/testing "Author can be empty, if editor isn't"
        (t/is (true? (source-spec/write-source-valid? no-author-book))))
      (t/testing "Both author and editor can't be empty"
        (t/is (false? (source-spec/write-source-valid? bad-book)))
        (t/testing "Spec tells us that author-book needs non-empty author, while
                    editor-book needs non-empty editor."
          (t/is (= (->> (source-spec/write-source-explain-data
                         (sut/book "dunham2022" nil nil "The Every Book"
                                   "Random House" 2022))
                        :cljs.spec.alpha/problems
                        (map :path)
                        set)
                   #{["book" :author-book :author]
                     ["book" :editor-book :editor]}))))))

  (t/testing "Manifesto, Conference, and Inbook are validated"
    (t/is (true? (source-spec/write-source-valid?
                  (sut/booklet "dunham2022" "My Manifesto"))))
    (t/is (true? (source-spec/write-source-valid?
                  (sut/conference "dunham2022" "Joel Dunham" "Some Conference"
                                  "The Proceedings of Some Conference" 2022))))
    (t/is (true? (source-spec/write-source-valid?
                  (sut/inbook "dunham2022" "Joel Dunham" "Editors & Sons"
                              "The Compendium" "13" "120-145" "Random House"
                              2022)))))

  (t/testing "A source without a valid type and key is invalid"
    (t/is (false? (source-spec/write-source-valid? sut/default-write-source)))
    (t/is (= "no method"
             (->> (source-spec/write-source-explain-data sut/default-write-source)
                  :cljs.spec.alpha/problems first :reason)))
    (t/is (true? (source-spec/write-source-valid? (assoc sut/default-write-source
                                                         :type "misc"
                                                         :key "k"))))))

(t/deftest author-in-citation-form-works
  (doseq [[source expected]
          {{:author "Ralph Alpher and Bethe, Hans and George Gamow"}
           "Alpher, Bethe and Gamow"
           {:editor "Ralph Alpher and Bethe, Hans and George Gamow" :author " "}
           "Alpher, Bethe and Gamow"
           {:editor "" :author " " :title "Great Article"} "Great Article"
           {} "no author"}]
    (t/is (= expected (sut/author-in-citation-form source)))))

(t/deftest year-works
  (doseq [[source expected]
          {{:year 2022} 2022
           {} -3000}]
    (t/is (= expected (sut/year source)))))

(t/deftest citation-works
  (t/testing "author-in-citation-form works"
    (t/is (= (repeat 3 "Dunham")
             [(sut/author-in-citation-form {:author "Dunham, Joel"})
              (sut/author-in-citation-form {:editor "Joel Dunham"})
              (sut/author-in-citation-form {:title "Dunham"})]))
    (t/is (= "no author" (sut/author-in-citation-form {}))))
  (t/testing "citation works"
    (t/is (= (repeat 3 "Dunham (2022)")
             [(sut/citation {:author "Dunham, Joel" :year 2022})
              (sut/citation {:editor "Joel Dunham" :year 2022})
              (sut/citation {:title "Dunham" :year 2022})]))
    (t/is (= "no author (-3000)" (sut/citation {})))
    (t/is (= "BranVan (-3000)" (sut/citation {:author "von BranVan, Jr., Ludwig"})))
    (t/is (= "no author (0)" (sut/citation {:year 0})))))
