(ns test.dativerf.utils-test
  (:require [cljs.test :as t :include-macros true]
            [dativerf.utils :as sut]))

(t/deftest ->kebab-case-recursive-works
  (t/is (= {:some-key 2
            :some-other-key
            [2
             {:another-key "abc"
              'symbolKey "def"}
             {:a-b {:c-dog-fish 444}}]}
           (sut/->kebab-case-recursive
            {:someKey 2
             :some_other_key
             [2
              {:AnotherKey "abc"
               'symbolKey "def"}
              {:a_b {:cDogFish 444}}]}))))

(t/deftest get-empty-string->nil-works
  (t/is (= (repeat 4 nil)
           [(sut/get-empty-string->nil {} :a)
            (sut/get-empty-string->nil {:a nil} :a)
            (sut/get-empty-string->nil {:a ""} :a)
            (sut/get-empty-string->nil {:a "   "} :a)]))
  (t/is (= "a" (sut/get-empty-string->nil {:a " a   "} :a))))

(t/deftest unicode-inspect-works
  (t/is (= [[{:unicode "U+0061", :name "LATIN SMALL LETTER A"}
             {:unicode "U+0301", :name "COMBINING ACUTE ACCENT"}]
            [{:unicode "U+0263", :name nil}]
            [{:unicode "U+019B", :name nil}
             {:unicode "U+0315", :name nil}]
            [{:unicode "U+0071", :name nil}
             {:unicode "U+0313", :name nil}
             {:unicode "U+02B7", :name nil}]]
           [(sut/unicode-inspect "á" {"0061" "LATIN SMALL LETTER A"
                                      "0301" "COMBINING ACUTE ACCENT"})
            (sut/unicode-inspect "ɣ")
            (sut/unicode-inspect "ƛ̕")
            (sut/unicode-inspect "q̓ʷ")])))

(t/deftest parse-comma-delimited-string-works
  (doseq [[input expected] [["" []]
                            ["     " []]
                            ["\n\n\t\t  " []]
                            ["a" ["a"]]
                            ["   a\n\n" ["a"]]
                            ["a,b,c, de      , ffalkj     " ["a" "b" "c" "de" "ffalkj"]]]]
    (t/is (= expected (sut/parse-comma-delimited-string input)))))

(t/deftest remove-enclosing-brackets-works
  (doseq [[input expected] [["a" "a"]
                            ["[a" "a"]
                            ["a]" "a"]
                            ["[a]" "a"]
                            ["[[a]]" "[a]"]]]
    (t/is (= expected (sut/remove-enclosing-brackets input)))))
