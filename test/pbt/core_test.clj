(ns pbt.core-test
  (:require [clojure.test :refer :all]
            [pbt.prop-utils :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [pbt.core :refer :all]))


(def lower-letter-generator
  (gen/fmap char (gen/choose 97 122)))

(check-prop only-lower-letter-prop [c]
  {:failed    (or (< (int c) (int \a))
                  (> (int c) (int \z)))
   :fail-info {:char c
               :int-value (int c)}})

(deftest ^:focused a-test
  (is (= nil (check (prop/for-all* [lower-letter-generator] only-lower-letter-prop)))))
