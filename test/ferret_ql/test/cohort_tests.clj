(ns ferret-ql.test.cohort-tests
  (:require [midje.sweet :refer :all]
        [ferret-ql.models.query-engine :as query-engine]
        [net.cgrand.parsley :as parsley]))

 (defmacro fact-skip [& r] `())

(def orders-test-data [{"data-added" "2010-01-01"  "user_id" 1  "amount_total" 10.50   "acquisition_source" "AA"}
                       {"data-added" "2010-01-03"  "user_id" 2  "amount_total" 5.20    "acquisition_source" "BB"}
                       {"data-added" "2010-02-01"  "user_id" 1  "amount_total" 7.40    "acquisition_source" "AA"}
                       {"data-added" "2010-03-01"  "user_id" 2  "amount_total" 14.00   "acquisition_source" "BB"}])


(fact-skip
"mogrify map in map"
(query-engine/demogrify-value 
  (query-engine/transmogrify-value
  {1 {"source" "AA"}}
   identity))
=> 
  {2 11
   1 10   
   4 [2 23 334]
   3 [1 22 333]})



(fact
  (query-engine/demogrify-value
    {:__trans-type :map,
     :__trans-value
     [[{:__trans-type :scalar, :__trans-value 2}
       {:__trans-type :map,
        :__trans-value
        [[{:__trans-type :scalar, :__trans-value "source"}
          {:__trans-type :scalar, :__trans-value "BB"}]]}]]})
  => {2 {"source" "BB"}})



(fact-skip
  (query-engine/demogrify-value
     {:__trans-type :map,
 :__trans-value
 [[{:__trans-type :scalar, :__trans-value [2 2]}
   {:__trans-type :map,
    :__trans-value
    [[{:__trans-type :scalar, :__trans-value "source"}
      {:__trans-type :scalar, :__trans-value "BB"}]]}]]}
)
  => {2 {"source" "BB"}})

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

  =>  [{1 {"source" "AA"}} {2 {"source" "BB"}}])

(fact-skip
  "acquisition source by user id - grouped"
  (query-engine/test-squery
    { "select" {"{order.user_id}"  { 
        "source" "(first {order.acquisition_source})" 
        }}
      "for"   "order"
      "in"    "_"
      "group" "1"
    }

    orders-test-data)

  => [{1 {"source" "AA"}, 2 {"source" "BB"}}])



(fact
  "aggragate orders by user id"
  (query-engine/test-squery
    { "select" {"{order.user_id}"  { 
        "orders" ["{order.amount_total}"] ;"{order.amount_total}"
        }}
      "for"   "order"
      "in"    "_"
      "group" "{order.user_id}"
    }

    orders-test-data)

  =>  [{1 {"orders" [10.5 7.4]}} {2 {"orders" [5.2 14.0]}}])

(comment 

  (fact-skip
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
)