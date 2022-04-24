(ns beam-x.schema.types-test
  (:require [clojure.test :refer :all])
  (:require [beam-x.schema.types :refer :all]
            [schema.core :as s]))

(s/with-fn-validation)

(defn- thrown? [f]
  (try
    (do (f) false)
    (catch Exception e true)))

(comment

  (thrown? #(/ 0 0))
  )

(deftest test-schemas!
  (testing "this is a vector of functions"
    (is (s/validate Fn #(+ 1 1)))
    (is (true? (thrown? #(s/validate Fn 1))))
    (is (true? (thrown? #(s/validate Fn "str"))))))

(comment

  (s/explain Fn)
  (s/validate Fn #(+ 1 1))

  (thrown? (s/validate Fn 1))
  (thrown? (s/validate Fn {})))