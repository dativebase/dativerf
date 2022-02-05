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
