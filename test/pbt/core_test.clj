(ns pbt.core-test
  (:require [clojure.test :refer :all]
            [pbt.prop-utils :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [pbt.core :refer :all]))

(check-prop example-prop [s]
            {:failed    (= "" s)
             :fail-info {:string s}})

(deftest ^:focused a-test
  (is (= nil (check
               (prop/for-all* [gen/string-ascii] example-prop)
               500
               6))))
