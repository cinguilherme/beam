(ns beam-x.schema.types
  (:require [schema.core :as s]))

(s/defschema Fn
  "A function with no arguments that will return something, usually a lambda
  wrapping a ready to fire function with args already in place."
  (s/=> s/Any))

(comment
  (s/with-fn-validation)

  (s/validate Fn #(+ 1 1))
  (s/validate Fn {}))