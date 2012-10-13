(ns ferret-ql.test.tests
  (:require [midje.sweet :refer :all]
  			[ferret-ql.models.query-engine :as query-engine]
        [net.cgrand.parsley :as parsley]))

 (defmacro fact-skip [& r] `())

  (fact
  "Test recursive object traversal"
  (query-engine/traverse-object 
    { 1 2
      3 {4 5}
      6 [7 8 [9 1 {2 3}]]}
    #(+ % 1))
=> {  2 3
      4 {5 6}
      7 [8 9 [10 2 {3 4}]]})


  (fact
    "Test parsley expression parser"
    (query-engine/parse-expr-into-parsley-Node "{x}")
    =>  #net.cgrand.parsley.Node{
          :tag :net.cgrand.parsley/root, 
          :content [#net.cgrand.parsley.Node{
              :tag :expr, 
              :content [#net.cgrand.parsley.Node{
                :tag :var, 
                :content ["{" #net.cgrand.parsley.Node{
                  :tag :word, 
                  :content ["x"]} 
                  "}"]}]}]}
    )


(fact
  "Test single-variable expression grammer parser"
  (query-engine/parse-expr "{test.aa}")
  => {:type :var
      :path ["test" "aa"]})

(fact
  "Test single-function expression grammer parser"
  (query-engine/parse-expr "(count {test.aa})")
  => {:type :func-call
      :func "count"
      :args [{:type :var
              :path ["test" "aa"]}]})

(fact
  "Test nested function expression grammer parser"
  (query-engine/parse-expr "(count {test.aa} {x.y.z} (+ {a}))")
  => {:type :func-call
      :func "count"
      :args [{:type :var
              :path ["test" "aa"]}
             {:type :var
              :path ["x" "y" "z"]}
             {:type :func-call
              :func "+"
              :args [{:type :var 
                      :path ["a"]}]}]})



(fact
  "Test find-first-and-rest"
  (query-engine/find-first-and-rest even? [1 3 4 1 2 3])
  => [4 [1 2 3]])


  

(fact
  "Test expression grammer parser on nested function expression"
  (query-engine/parse-expr "(+ (count {test.aa.bb}) {test2})")
  => {:type :func-call
      :func "+"
      :args [{:type :func-call
               :func "count"
               :args [{:type :var
                        :path ["test" "aa" "bb"]}]}
              {:type :var
               :path ["test2"]}]}
  )

(fact
  "Test expression grammer parser non-expression"
  (query-engine/parse-expr "test 4")
  => "test 4")


  (fact
    "Test simple eval-expr-var"
    (query-engine/eval-expr-var 
      {:type :var
       :path ["x" "a"]}
      
      [{"_index_" 0, "x" {"a" 1}, "_" [{"a" 1} {"a" 3} {"a" 7}]}, 
       {"_index_" 1, "x" {"a" 3}, "_" [{"a" 1} {"a" 3} {"a" 7}]}, 
       {"_index_" 2, "x" {"a" 7}, "_" [{"a" 1} {"a" 3} {"a" 7}]}])
    
    => `(1 3 7))

  (fact
    "Test simple eval-expr-var-single-context"
    (query-engine/eval-expr-var-single-context
      ["x" "a"]      
      {"_index_" 0, "x" {"a" 1}, "_" [{"a" 1} {"a" 3} {"a" 7}]})
    
    => 1)



  


  (fact
    "Test simple eval expression"
    (query-engine/eval-expr "{obj.prop1}" [{"obj" { "prop0" 10 "prop1" "42"}}])
    => ["42"]
  )

  (fact
    "Test static expression"
    (query-engine/eval-expr "stuff" [{"obj" { "prop0" 10 "prop1" 42}}])
    => "stuff"
  )

(comment

  (fact
    "Test compound eval expression"
    (query-engine/eval-expr "$obj.prop1 is $obj.prop0.abc" [{"obj" { "prop0" {"abc" "ok"} "prop1" "42"}}])
    => "42 is ok"
  )

)


  (fact
  "Test get-for-contexts"
  (query-engine/get-for-contexts {"for" "x" "in" "_"} {"_" [{:a 1} {:a 3} {:a 7}]})
=> [
    { "x" {:a 1}, "_" [{:a 1} {:a 3} {:a 7}]}
    { "x" {:a 3}, "_" [{:a 1} {:a 3} {:a 7}]}
    { "x" {:a 7}, "_" [{:a 1} {:a 3} {:a 7}]}    
  ])



(fact
  "Test group-contexts"
  (query-engine/group-contexts
    {"for" "x" "in" "_" "group" "{_index_}"} 
    [{ "x" {:a 1}, "_" [{:a 1} {:a 3} {:a 7}]}
     { "x" {:a 3}, "_" [{:a 1} {:a 3} {:a 7}]}
     { "x" {:a 7}, "_" [{:a 1} {:a 3} {:a 7}]}])
=> '([{"_index_" 0, "x" {:a 1}, "_" [{:a 1} {:a 3} {:a 7}]}], 
     [{"_index_" 1, "x" {:a 3}, "_" [{:a 1} {:a 3} {:a 7}]}], 
     [{"_index_" 2, "x" {:a 7}, "_" [{:a 1} {:a 3} {:a 7}]}]))








(comment

  (fact
  "Test complex select object context evaluation"
  (query-engine/eval-select-value
     {
      "$obj1.prop0.key1" "$obj1.prop0.prop1"
      "key$obj2.key" ["$obj3.key0" "$obj3.key1" {"$obj3.key0" "$obj3.key1"}]
      "test" "42"
     } 
     [{
      "_" [{:a 1} {:a 3} {:a 7}]
      "obj1" {
        "prop0" {"key1" "ABC"
                 "prop1" "DEF"}}
      "obj2" { "key" "GHI" }
      "obj3" {
        "key0" "JKL"
        "key1" "MNO"}}]
  )
=> {
    "ABC" "DEF"
    "keyGHI" ["JKL" "MNO" {"JKL" "MNO"}]
    "test" "42"
   } 
)

)

  (fact
  "Test simple select object context evaluation"
  (query-engine/eval-select-value
     "{x.prop1}"
     [{
      "_" [{:a 1} {:a 3} {:a 7}]
      "x" { "prop1" "42"}
      "obj2" { "key" "GHI" }
      "obj3" {
        "key0" "JKL"
        "key1" "MNO"}}]
  )
=> ["42"]
)
 
  (fact
  "Test simple select object multi group context evaluation"
  (query-engine/eval-select-value
     "{x.prop1}"
     [{"_" [{:a 1} {:a 3} {:a 7}]
      "x" { "prop1" "42"}}
      {"_" [{:a 2} {:a 5} {:a 9}]
      "x" { "prop1" "ff"}}
            ]
  )
=> ["42" "ff"]
)
 
 

(fact
    "***test-query -- Test confusing idea #1"
    (query-engine/test-query
    { "select"  {"{x.prop1}" "{x.prop2}"}
      "for"   "x"
      "in"    "_"
      "group" "1"}

    [{"prop1" "k11", "prop2" "v222"}
     {"prop1" "k33", "prop2" "v444"}])

  => [{"11" "222"
       "33" "444"}])

  (fact
    "test-query -- Test confusing idea #2"
    (query-engine/test-query
    { "select"  {"aa" ["{x.prop1}" "{x.prop2}"]}
      "for"   "x"
      "in"    "_"
      "group" "1" }
      
    [{"prop1" "11", "prop2" "222"}
     {"prop1" "33", "prop2" "444"}])

  => [{ "aa" ["11" "222" "33" "444"]}])


  (fact
    "test-query -- Test confusing ideas together"
    (query-engine/test-query
    { "select"  {"array"      ["{x.prop1}" "{x.prop2}"]
                 "keys"       {"{x.prop1}" "{x.prop2}"}
                 "aggregate"  "(sum {x.prop1})"}
      "for"   "x"
      "in"    "_"
      "group" "1"}

    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}]

    )
  => [{ "array"     [11 222 33 444] 
        11          222
        33          444
        "aggregate" 44}])

  
  (fact
  "test-query -- Test selects single property from object"
  (query-engine/test-query
    {
      "select"  "{x.prop1}"
      "for"   "x"
      "in"    "_"
    }

    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [11 33])

  
  (fact
  "test-query -- Test selects single property from object - aggregated group w/o function?"
  (query-engine/test-query
    {
      "select"  "{x.prop1}"
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [11])

  (fact
  "test-query -- Test selects single property from object - grouped"
  (query-engine/test-query
    {
      "select"  ["{x.prop1}"]
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[11 33]])


  (fact
  "test-query -- Test selects multiple properties from nested objects in array"
  (query-engine/test-query
    { "select"  { "key" "{x.prop1}", "{x.prop2.key1}" "{x.prop2.prop3}" }
      "for"   "x"
      "in"    "_"}

    [{"prop1" "11", "prop2" {"key1" "22" "prop3" "33"}}
     {"prop1" "AA", "prop2" {"key1" "BB" "prop3" "CC"}}
     {"prop1" "22", "prop2" {"key1" "44" "prop3" "66"}}])

  => [{"key" "11", "22" "33"}
      {"key" "AA", "BB" "CC"}
      {"key" "22", "44" "66"}])


(comment
  
  (fact
  "Test count aggregate function"
  (query-engine/test-query
    { "select"  "(count $x)"
      "for"   "x"
      "in"    "_"
      "group" "1"
    }

    ["2", "3", "4", "5"]

    )
  => [4])



(fact
  "Test selects simple property correctly"
  (query-engine/test-query
    {"select" "{prop1}"}
    {"prop1" 22, "prop2" 34})
  => 22)

)
