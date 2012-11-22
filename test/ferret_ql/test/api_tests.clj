(ns ferret-ql.test.api-tests
  (:require [midje.sweet :refer :all]
            [noir.core :refer :all]
            [noir.util.test :refer :all]
            [ferret-ql.views.ferret-ql-api  :refer :all]))

(fact
"Test fail"
(+ 1 1)
=>  3)

(fact
 "Test testget"
 (:body (send-request "/testget/22"))
 => "[\"test\",\"22\"]")
