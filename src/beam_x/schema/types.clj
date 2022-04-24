(ns beam-x.schema.types
  (:require [schema.core :as s]))

(s/defschema Fn (s/=> s/Any))

(comment
  (s/with-fn-validation)

  (s/validate Fn #(+ 1 1))
  (s/validate Fn {})

  )