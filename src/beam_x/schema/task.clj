(ns beam-x.schema.task
  (:require [schema.core :as s]
            [clojure.core.async :as a :refer [chan >! <! go go-loop]]
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

(defn get-cat-fact-go [out]
  (go
    (println "calling cat fact")
    (>! out (get-cat-fact!))))

(defn get-cat-fact-parked [col]
  (let [out (chan 10)]

    (loop [c col]
      (if (empty? c)
        c
        (do (get-cat-fact-go out)
          (recur (rest c)))))

    (a/go-loop []
      (let [x (<! out)]
        (if (nil? x)
          col
          (recur (conj col x)))))))

(let [out (get-cat-fact-parked [1 2])]
  (a/<!! out))

(defn all-parking [colf])


(comment
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
