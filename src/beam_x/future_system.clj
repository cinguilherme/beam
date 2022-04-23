(ns beam-x.future-system
  (:require [beam-x.schema.types :as t]
            [beam-x.utils :as u]))

(defn- all-future! [colf]
  (mapv #(future (%)) colf))

(defn- join-all! [colf]
  (mapv (fn [fp] @fp) colf))

(defn brute-threads!
  [colf]
  (beam-x.future-system/join-all!
     (beam-x.future-system/all-future!
       colf)))