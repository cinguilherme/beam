(ns beam-x.future-system
  (:require [schema.core :as s]
            [beam-x.schema.types :as t]
            [beam-x.utils :as u]))

(defn- all-future!
  [colf]
  (mapv #(future (%)) colf))

(defn- join-all!
  [colf]
  (mapv (fn [fp] @fp) colf))

(defmacro brute-threads
  [colf]
  `(join-all!
     (all-future!
       ~colf)))

(comment
  (def cats (mapv (fn [_] u/get-cat-fact!) (range 10)))

  (macroexpand '(brute-threads cats))
  (time (brute-threads cats)))