(ns pbt.prop-utils
  (:require [clojure.test.check :as tc]))

(defmulti check-fail class)
(defmethod check-fail Boolean [b] {:failed (not b)})
(defmethod check-fail java.util.Map [m] m)

(defn make-meta-info [f-meta params keyed-params]
  (reduce (fn [m [k v]] (assoc-in m [:params (keyword k)] v))
          {:f      f-meta
           :params {}}
          (partition 2 (interleave keyed-params params))))

(defmacro check-prop [sym params & body]
  (let [keyed-params (mapv keyword params)]
    `(defn ~sym ~params
       (let [fail# (check-fail (do ~@body))]
         (if-let [result# (not (:failed fail#))]
           result#
           (throw
             (ex-info (str "Property Fail:")
                      (merge fail#
                             (make-meta-info (meta (var ~sym))
                                             ~params
                                             ~keyed-params)))))))))

(defn java-ex-data [m ex]
  (if (and (instance? Exception ex)
           (not= "Property Fail:" (.getMessage ex)))
    (-> m
        (assoc :exception {})
        (assoc-in [:exception :cause] (map str (seq (.getStackTrace ex))))
        (assoc-in [:exception :class] (class ex))
        (assoc-in [:exception :msg] (.getMessage ex)))
    m))

(defn check
  ([property]
   (check property 100))
  ([property num-tests]
   (check property num-tests 30))
  ([property num-tests max-size]
   (check property num-tests max-size nil))
  ([property num-tests max-size seed]
   (let [result (if seed
                  (tc/quick-check num-tests
                                  property
                                  :seed seed
                                  :max-size max-size)
                  (tc/quick-check num-tests
                                  property
                                  :max-size max-size))]
     (if (:fail result)
       (-> result
           (dissoc :result)
           (update :shrunk #(dissoc % :result))
           (assoc :at (-> (or (ex-data (:result result)) {})
                          (java-ex-data (:result result)))))))))
