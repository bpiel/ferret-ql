(ns ferret-ql.test.api-tests
  (:require [midje.sweet :refer :all]
            [noir.core :refer :all]
            [noir.util.test :refer :all]))

(fact
 "Test testget"
 (:body (send-request "/testget/22"))
 => "[\"test\",\"22\"]")

  (fact
  "Test JSON query endpoint"
  (:body (send-request "/test-query"
    {:query
    "{ \"select\": {\"sum\" : \"(sum (merge {x.a}))\",
                  \"count\" : \"(count (merge {x.a}))\",
                  \"b\":     \"(first {x.b})\"
              },
      \"for\" : \"x\",
      \"in\" :  \"_\",
      \"group\"  : [\"{x.b}\", \"{x.c}\"]
    }"
    :data
    "[{\"a\": [1, 2],  \"b\": 1, \"c\": 1},
      {\"a\": [3],    \"b\": 1, \"c\": 1},
      {\"a\": [4, 5],  \"b\": 2, \"c\": 1},
      {\"a\": [6],    \"b\": 2, \"c\": 2}]"}))

  =>  "[{\"b\":1,\"sum\":6,\"count\":3},{\"b\":2,\"sum\":9,\"count\":2},{\"b\":2,\"sum\":6,\"count\":1}]")

