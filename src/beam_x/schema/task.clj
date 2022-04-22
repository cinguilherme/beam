(ns beam-x.schema.task
  (:require [schema.core :as s]
            [clojure.core.async :as a :refer [chan >! <!! <! go go-loop]]
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
  (let [max-size 50
        c (count col)]
    (if (< c max-size)
      c
      max-size)))

(defn get-cat-fact-parked [colf]
  (let [out (chan (chan-size colf))

        inx (go-loop [cx colf]
              (if (empty? cx)
                (do (println "no more to input"))
                (do (>! out (get-cat-fact!))
                    (recur (rest cx)))))

        outx (go-loop [col []]
               (if-some [v (<! out)]
                 (recur (conj col v))))]

    (do (println outx)
        outx)))

(comment

  (def many-cat-facts (mapv #(get-cat-fact!) (range 20)))

  (get-cat-fact-parked many-cat-facts)


  (defmacro tap [v]
    `(do
       (println ~v)
       ~v))

  (defn get-cat-fact! []
    (-> (slurp "https://catfact.ninja/fact") parse-string keywordize-keys))


  (macroexpand '(brute-threads [get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact!]))

  (get-cat-fact!)

  (brute-threads [get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact! get-cat-fact!])

  )
