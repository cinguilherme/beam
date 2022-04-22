(ns beam-x.schema.task
  (:require [clojure.core.async :as a :refer [chan >! <! <!! >!! go go-loop close!]]
            [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [keywordize-keys]]))

(defn all-future! [colf]
  (mapv #(future (%)) colf))

(defn join-all! [colf]
  (mapv (fn [fp] @fp) colf))

(defmacro brute-threads [colf]
  `(join-all!
     (all-future!
       ~colf)))

(defn get-cat-fact! []
  (-> (slurp "https://catfact.ninja/fact") parse-string keywordize-keys))

(defn chan-size [col]
  (let [max-size 5
        c (count col)]
    (if (< c max-size)
      c
      max-size)))

(defn go-wrapped-f-into-chan [f out]
  (go (>! out (f))))

(defn ingest-system [in out]
  (go-loop []
    (when-some [f (<! in)]
      (do
        (go-wrapped-f-into-chan f out)
        (recur)))))

(defn drain-system! [in out]
  (go-loop []
    (when-some [v (<! in)]
      (>! out v)
      (recur))))

(defn create-parking-system []
  (let [in (chan)
        out-i (chan)
        out-l (chan)
        is (ingest-system in out-i)
        ds (drain-system! out-i out-l)]
    [in out-i out-l is ds]))

(defn run-parking-system! [system colf]
  (let [[in out-i out-l is ds] system]
    (mapv #(>!! in %) colf)

    (let [r (loop [c []]
              (let [v (<!! out-l)]
                (if (or (= (count c) (dec (count colf))) (nil? v))
                  c
                  (recur (conj c v)))))]
      (mapv #(close! %) [in out-i out-l is ds])
      r)))

(defn parking-brute [colf]
  (let [out (chan (chan-size colf))

        _ (go-loop [cx colf]
            (if (empty? cx)
              (a/close! out)
              (do (>! out ((first cx)))
                  (recur (rest cx)))))]

    (loop [col []]
      (let [v (<!! out)]
        (if (nil? v)
          col
          (recur (conj col v)))))))

(comment

  (def many-cat-facts (mapv (fn [_] get-cat-fact!) (range 10)))
  (println many-cat-facts)

  (time (parking-brute many-cat-facts))

  (def parking-system (create-parking-system))

  (time (run-parking-system! parking-system many-cat-facts))


  (defmacro tap [v]
    `(do
       (println ~v)
       ~v))

  (defn get-cat-fact! []
    (-> (slurp "https://catfact.ninja/fact") parse-string keywordize-keys))


  (macroexpand '(brute-threads many-cat-facts))

  (get-cat-fact!)

  (time (brute-threads many-cat-facts))

  )
