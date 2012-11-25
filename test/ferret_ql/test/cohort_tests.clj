(ns ferret-ql.test.cohort-tests
  (:require [midje.sweet :refer :all]
        [ferret-ql.models.query-engine :as query-engine]
        [net.cgrand.parsley :as parsley]))

 (defmacro fact-skip [& r] `())


(fact
"+ 1 1"
(+ 1 1)
=>  3)
