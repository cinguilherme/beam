(ns beam-x.future-system
  (:require [beam-x.schema.types :as t]
            [schema.core :as s]))

(defn- all-future! [col]
  (mapv #(future (%)) col))

(defn- join-all! [col]
  (mapv (fn [fp] @fp) col))

(s/defn brute-threads! :- [s/Any]
  "Simplest of them all. For each function in col a future thread
   with be fired, all results will be joined in the end
  and provided back as a vector of the results.
  Use this if the col is small enough that the number of threads
  fired can be handled from the cpu/memory perspective.
  eg: Cpu intensive operations a small number roughly equal to the number of CPU cores available will be a fit.
  IO blocking operations like network calls can be a good fit for the hundreds as well, but at the cost of memory for the threads."
  [col :- [t/Fn]]
  (beam-x.future-system/join-all!
    (beam-x.future-system/all-future!
      col)))