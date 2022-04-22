(ns beam-x.schema.task
  (:require [clojure.core.async :as a :refer [chan >! <!  <!! >!! go go-loop close!]]
            [cheshire.core :as ches :refer [parse-string]]
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

(defn ingest-system [in out]
  (go-loop []
    (when-some [f (<! in)]
      (do (println "ingesting function f" f)
        (>! out (f))
          (recur)))))

(defn drain-system [in out]
  (go-loop []
    (when-some [v (<! in)]
      (>! out v)
      (recur))))

(defn create-parking-system []
  (let [in (chan 100)
        out-i (chan 5)
        out-d (chan 5)
        is (ingest-system in out-i)
        ds (drain-system out-i out-d)]
    (println "parking system created")
    [in out-i out-d is ds]))

(defn run-parking-systems [colf]
  (let [[in _ out-d _ _] (create-parking-system)]
    (println "mapping functions into in chan")
    (mapv #(>!! in %) colf)
    (println "completed!")

    (<!! out-d)))

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

  (def many-cat-facts (mapv (fn [_] get-cat-fact!) (range 5)))

  (time (parking-brute many-cat-facts))

  (run-parking-systems many-cat-facts)


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
