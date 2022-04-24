(ns beam-x.parking-system-test
  (:require [clojure.test :refer :all])
  (:require [beam-x.parking-system :refer :all]
            [beam-x.utils :as u]
            [schema.core :as s]))

(s/with-fn-validation)

(deftest concur-parking!-test

  (testing "get all results back"
    (is (= [1 2 4]
           (sort
             (concur-parking! [#(+ 1 0) #(+ 1 1) #(+ 2 2)])))))

  (testing "vast functions"
    (let [fnz (map (fn [v] #(identity v)) (range 1000))]
      (is (= (range 1000)
             (sort (concur-parking! fnz))))))

  (testing "vast functions"
    (let [fnz (map (fn [v] #(identity v)) (range 10000))]
      (is (= (range 10000)
             (sort (concur-parking! fnz))))))
  )

(comment

  (def cats (mapv (fn [_] u/get-cat-fact!) (range 10)))

  (concur-parking! cats)

  (concur-parking! [#(+ 1 0) #(+ 1 1) #(+ 2 2) #(+ 2 2)])
  )