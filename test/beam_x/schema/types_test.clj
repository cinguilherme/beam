(ns beam-x.schema.types-test
  (:require [clojure.test :refer :all])
  (:require [beam-x.schema.types :refer :all]
            [schema.core :as s]
            [beam-x.utils :as u :refer [throw?]]))

(s/with-fn-validation)

(comment

  (thrown? #(/ 0 0))
  )

(deftest test-schemas!
  (testing "this is a vector of functions"
    (is (s/validate Fn #(+ 1 1)))
    (is (true? (throw? #(s/validate Fn 1))))
    (is (true? (throw? #(s/validate Fn "str"))))))

(comment

  (s/explain Fn)
  (s/validate Fn #(+ 1 1))

  (thrown? (s/validate Fn 1))
  (thrown? (s/validate Fn {})))