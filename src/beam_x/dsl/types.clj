(ns beam-x.dsl.types
  (:require [schema.core :as s]))

(def operation-types
  #{:cpu :file-io :network :memory})

(s/defschema Operation
  {:type   s/Keyword
   :col-fn [(s/=> s/Any)]})

(comment

  (s/with-fn-validation)
  (s/validate Operation {:type   :cpu
                         :col-fn [#(+ 1 1)]})

  )