(ns beam-x.future-system-test
  (:require [clojure.test :refer :all])
  (:require [beam-x.future-system :refer :all]
            [beam-x.utils :as u]))

(deftest brute-threads!-test
  (testing "all results as collection"
    (is (= [2 4])
        (brute-threads! [#(+ 1 1) #(+ 2 2)])))
  (testing "vast functions"
    (let [fnz (map (fn [v] #(identity v)) (range 1000))]
      (is (= (range 1000)
             (sort (brute-threads! fnz)))))))


(comment
  (def cats (mapv (fn [_] u/get-cat-fact!) (range 10)))

  (macroexpand '(brute-threads! cats))
  (time (brute-threads! cats))
  (time (brute-threads! [#(+ 1 1)]))
  )
