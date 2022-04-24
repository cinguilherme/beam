(ns beam-x.utils
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [keywordize-keys]]))

(defn get-cat-fact! []
  (->
    (slurp "https://catfact.ninja/fact")
    parse-string
    keywordize-keys))

(defn throw? [f]
  (try
    (do (f) false)
    (catch Exception e true)))

(comment
  (get-cat-fact!)
  )