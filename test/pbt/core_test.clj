(ns pbt.core-test
  (:require [clojure.test :refer :all]
            [pbt.prop-utils :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [pbt.core :refer :all]))

; production

; char -> int
(defn ordinal-lower-letter [c]
  (- (int c) (int \a)))

(defn ordinal-to-char [ord]
  (char (+ ord (int \a))))

(defn ordinals-of-diamond [c]
  (concat
    (range (ordinal-lower-letter \a)
           (ordinal-lower-letter c))
    [(ordinal-lower-letter c)]
    (reverse (range (ordinal-lower-letter \a)
                    (ordinal-lower-letter c)))))

; char -> [String]
(defn diamond [c]
  (map #(str % " ")
       (map ordinal-to-char (ordinals-of-diamond c))))

; test

; many tests. it seems we can make smaller steps with properties than with examples.
; with examples there are 3 tests and we are done.

(def lower-letter-generator
  "Generate lowercase characters"
  (gen/fmap char (gen/choose 97 122)))

(check-prop only-lower-letter-prop [c]
  {:failed    (or (< (int c) (int \a))
                  (> (int c) (int \z)))
   :fail-info {:char      c
               :int-value (int c)}})

(check-prop height-is-2x+1-prop [c]
  (let [expected-height (inc (* 2 (ordinal-lower-letter c)))]
    {:failed    (not= (count (diamond c))                   ; styling: split after 2nd element of list
                      expected-height)
     :fail-info {:char            c
                 :int-value       (int c)
                 :actual-height   (count (diamond c))
                 :expected-height expected-height}}))

(def lower-letter-generator-without-a
  "Generate lowercase characters"
  (gen/fmap char (gen/choose 98 122)))

(defn lines-with-not-2-chars [xs]
  (filter #(not= 2 (count %))
          (map set xs)))

(check-prop line-contains-exactly-two-letters [c]
  {:failed (seq (lines-with-not-2-chars (diamond c)))})

(defn lines-without-whitespaces [xs]
  (filter #(not (contains? % \space))
          (map set xs)))

(check-prop line-contains-whitespaces [c]
  {:failed (seq (lines-without-whitespaces (diamond c)))})

(def inc-for-whitespace inc)

(defn characters-of [lines]
  (reduce (partial merge-with +)
          (map frequencies lines))
  )

; dropped test because of Peter being evil changing code to what the test aks us.
(deftest letters-of-diamond-test
  (is (= [0 1 0] (ordinals-of-diamond \b)))
  )

(deftest ^:focused characters-of-test
  ; explore the language in tests
  (is (= {\t 1} (frequencies "t")))
  (is (= {\t 2} (merge-with + {\t 1} {\t 1})))
  )

(check-prop diamond-contains-all-characters [c]
  {:failed    (not= (count (characters-of (diamond c)))
                    (inc-for-whitespace (inc (ordinal-lower-letter c))))
   :assertion {:got      (count (characters-of (diamond c)))
               :expected (inc-for-whitespace (inc (ordinal-lower-letter c)))}
   :fail-info {:x-characters-of (characters-of (diamond c))}
   })

(check-prop diamond-is-mirrored-on-horizontal-axis [c]
  {:failed    (not= (diamond c)
                    (reverse (diamond c)))
   ; the expected output is not helpful because this is not an example. the expected also looks wrong.
   :assertion {:got      (reverse (diamond c))
               :expected (diamond c)}
   })

; custom runner

; TODO create example for a, remove lower-letter-generator and test group.
(deftest ^:focused all-letters-tests
  (is (= nil (check (prop/for-all* [lower-letter-generator]
                                   #(and (only-lower-letter-prop %)
                                         (height-is-2x+1-prop %)))))))

(deftest ^:focused no-a-letters-tests
  (is (= nil (check (prop/for-all* [lower-letter-generator-without-a]
                                   #(and (only-lower-letter-prop %)
                                         (height-is-2x+1-prop %)
                                         (line-contains-exactly-two-letters %)
                                         (line-contains-whitespaces %)
                                         (diamond-contains-all-characters %)
                                         (diamond-is-mirrored-on-horizontal-axis %)))))))
