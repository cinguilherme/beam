(ns beam-x.parking-system
  (:require [clojure.core.async :as a :refer [chan go go-loop >! <! >!! <!! close!]]
            [schema.core :as s]
            [beam-x.schema.types :as t]))

(defn- go-wrapped-f-into-chan [f out]
  (go (>! out (f))))

(defn- ingest-system [in out]
  (go-loop []
    (when-some [f (<! in)]
      (do
        (go-wrapped-f-into-chan f out)
        (recur)))))

(defn- drain-system! [in out]
  (go-loop []
    (when-some [v (<! in)]
      (>! out v)
      (recur))))

(defn- create-parking-system []
  (let [in (chan)
        out-i (chan)
        out-l (chan)
        is (ingest-system in out-i)
        ds (drain-system! out-i out-l)]
    [in out-i out-l is ds]))

(defn- run-parking-system!
  [system col]
  (let [[in out-i out-l is ds] system
        size (count col)]
    (mapv #(>!! in %) col)

    (let [r (loop [c []]
              (let [v (<!! out-l)]
                (if (or (nil? v) (= (count c) (dec size)))
                  (conj c v)
                  (recur (conj c v)))))]
      (mapv #(close! %) [in out-i out-l is ds])
      r)))

(defn- concur-parking-part!
  [col-partition]
  (let [system (create-parking-system)]
    (run-parking-system! system col-partition)))

(s/defn concur-parking! :- [s/Any]
  "The parking system aims to be a bit more lightweight than the threads option.
  This will use the clojure/core.async tools to resolve the results in a vector.
  Order not Guaranteed here.
  Allowing for possibly a lot larger collection of functions
  to be in-taken and backpressure will be guaranteed.
  However, bad fit for CPU intensive tasks."
  [col :- [t/Fn]]
  (let [parts (partition 1000 1000 nil col)]
    (loop [p parts res []]
      (if (empty? p)
        res
        (recur (rest p) (concat res (concur-parking-part! (first p))))))))
