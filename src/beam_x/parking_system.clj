(ns beam-x.parking-system
  (:require [clojure.core.async :as a :refer [chan go go-loop >! <! >!! <!! close!]]))

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
  [system colf]
  (let [[in out-i out-l is ds] system
        size (count colf)]
    (mapv #(>!! in %) colf)

    (let [r (loop [c []]
              (let [v (<!! out-l)]
                (if (or (nil? v) (= (count c) (dec size)) )
                  (conj c v)
                  (recur (conj c v)))))]
      (mapv #(close! %) [in out-i out-l is ds])
      r)))

(defn- concur-parking-part!
  [colf-p]
  (let [system (create-parking-system)]
    (run-parking-system! system colf-p)))

(defn concur-parking!
  [colf]
  (let [parts (partition 1000 1000 nil colf)]
    (loop [p parts res []]
      (if (empty? p)
        res
        (recur (rest p) (concat res (concur-parking-part! (first p))))))))
