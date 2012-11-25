(ns ferret-ql.test.cohort-tests
  (:require [midje.sweet :refer :all]
        [ferret-ql.models.query-engine :as query-engine]
        [net.cgrand.parsley :as parsley]))

 (defmacro fact-skip [& r] `())

(def orders-test-data [{"data-added" "2010-01-01"  "user_id" 1  "amount_total" 10.50   "acquisition_source" "AA"}
                       {"data-added" "2010-01-03"  "user_id" 2  "amount_total" 5.20    "acquisition_source" "BB"}
                       {"data-added" "2010-02-01"  "user_id" 1  "amount_total" 7.40    "acquisition_source" "AA"}
                       {"data-added" "2010-03-01"  "user_id" 2  "amount_total" 14.00   "acquisition_source" "BB"}])

(fact
  "acquisition source by user id"
  (query-engine/test-squery
    { "select" {"{order.user_id}"  { 
        "source" "(first {order.acquisition_source})" 
        }}
      "for"   "order"
      "in"    "_"
      "group" "{order.user_id}"
    }

    orders-test-data)

  => [])

(fact
  "aggragate orders by user id"
  (query-engine/test-squery
    { "select" {"{order.user_id}"  { 
        "orders" "(merge {order.amount_total})"
        }}
      "for"   "order"
      "in"    "_"
      "group" "{order.user_id}"
    }

    orders-test-data)

  => [])

(fact
  "orders and acquisition_source by user id"
  (query-engine/test-squery
    { "select" {"{order.user_id}"  { 
        "source" "(first {order.acquisition_source})" 
        "orders" ["{order.amount_total}"]
        }}
      "for"   "order"
      "in"    "_"
      "group" "{order.user_id}"
    }

    orders-test-data)

  => [])