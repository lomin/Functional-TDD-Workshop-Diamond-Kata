(ns pbt.core-test
  (:require [clojure.test :refer :all]
            [pbt.prop-utils :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [pbt.core :refer :all]))

; production

(defn ordinal-lower-letter [c]
  (- (int c) (int \a)))

(defn diamond [c]
  (repeat (+ 1 (* 2 (ordinal-lower-letter c)))
          "a "))

; test

(def lower-letter-generator
  "Generate lowercase characters"
  (gen/fmap char (gen/choose 97 122)))

(def lower-letter-generator-without-a
  "Generate lowercase characters"
  (gen/fmap char (gen/choose 98 122)))

(check-prop only-lower-letter-prop [c]
  {:failed    (or (< (int c) (int \a))
                  (> (int c) (int \z)))
   :fail-info {:char      c
               :int-value (int c)}})

(check-prop height-is-2x-1-prop [c]
  {:failed    (not= (count (diamond c))
                    (inc (* 2 (ordinal-lower-letter c))))
   :fail-info {:char      c
               :int-value (int c)
               :height    (count (diamond c))}})

(defn lines-with-not-2-chars [xs]
  (filter #(not= 2 (count %)) (map set xs)))

(defn lines-without-whitespaces [xs]
  (filter #(not (contains? % \space)) (map set xs)))

(check-prop line-contains-exactly-two-letters [c]
  {:failed (seq (lines-with-not-2-chars (diamond c)))})

(check-prop line-contains-whitespaces [c]
  {:failed (seq (lines-without-whitespaces (diamond c)))})

; custom runner

(deftest ^:focused a-test
  (is (= nil (check (prop/for-all* [lower-letter-generator]
                                   #(and (only-lower-letter-prop %)
                                         (height-is-2x-1-prop %))))))
  (is (= nil (check (prop/for-all* [lower-letter-generator-without-a]
                                   #(and
                                      (line-contains-exactly-two-letters %)
                                      (line-contains-whitespaces %)))))))
