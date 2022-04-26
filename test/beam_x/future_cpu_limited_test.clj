(ns beam-x.future_cpu_limited-test
  (:require [clojure.test :refer :all])
  (:require [beam-x.future_cpu_limited :refer :all]))

(def vec-f (mapv (fn [v] #(do
                            (Thread/sleep 200)
                            (inc v))) (range 40)))

(def expectation (mapv inc (range 40)))

(deftest cpu-limited-batching-pipeline-test!
  (testing "works just the same, but wont clog the entire system with threads"
    (is (= expectation
           (vec (batching-pipeline vec-f))))))