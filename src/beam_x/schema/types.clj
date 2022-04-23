(ns beam-x.schema.types
  (:require [schema.core :as s]))

(s/defschema Fn (s/=> s/Any))