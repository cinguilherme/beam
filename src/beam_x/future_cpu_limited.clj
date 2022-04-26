(ns beam-x.future_cpu_limited
  (:require [beam-x.dsl.types :as dsl.types]
            [schema.core :as s]
            [beam-x.future-system :as future-system]))

(def n-cpu (.availableProcessors (Runtime/getRuntime)))

(defn batching-pipeline [col]
  (let [parts (partition n-cpu n-cpu nil col)]
    (loop [c parts res []]
      (if (or (nil? c) (empty? c))
        res
        (let [valz (future-system/brute-threads! (first c))]
          (recur (rest c) (concat res valz)))))))

(comment
  (defn cpu-intense [] (do (Thread/sleep 1500)
                           "done!"))
  (def colz (mapv (fn [_] #(cpu-intense)) (range 20)))

  (batching-pipeline colz))