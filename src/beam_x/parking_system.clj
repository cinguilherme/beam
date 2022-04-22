(ns beam-x.parking-system
  (:require [clojure.core.async :as a :refer [chan go go-loop >! <! >!! <!! close!]]
            [beam-x.utils :as u]))

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

(defn- run-parking-system! [system colf]
  (let [[in out-i out-l is ds] system]
    (mapv #(>!! in %) colf)

    (let [r (loop [c []]
              (let [v (<!! out-l)]
                (if (or (= (count c) (dec (count colf))) (nil? v))
                  c
                  (recur (conj c v)))))]
      (mapv #(close! %) [in out-i out-l is ds])
      r)))

(defn concur-parking! [colf]
  (let [system (create-parking-system)]
    (run-parking-system! system colf)))

(comment

  (def cats (mapv (fn [_] u/get-cat-fact!) (range 10)))

  (concur-parking! cats)
  )