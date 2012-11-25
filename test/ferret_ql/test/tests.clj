(ns ferret-ql.test.tests
  (:require [midje.sweet :refer :all]
  			[ferret-ql.models.query-engine :as query-engine]
        [net.cgrand.parsley :as parsley]))

 (defmacro fact-skip [& r] `())


(fact-skip
"Test transmogrify-value scalar"
(query-engine/transmogrify-value
  1
  #(+ % 1))
=>  {:__trans-type :scalar, :__trans-value 2})

(fact-skip
"Test demogrify-value scalar"
(query-engine/demogrify-value
  {:__trans-type :scalar, :__trans-value 2})
=>  2)

(fact-skip
"Test transmogrify-value map"
(query-engine/transmogrify-value
  {1 10 3 15}
  #(+ % 1))
=> 
  {:__trans-type :map, 
   :__trans-value '(
    [{:__trans-type :scalar, :__trans-value 2} {:__trans-type :scalar, :__trans-value 11}] 
    [{:__trans-type :scalar, :__trans-value 4} {:__trans-type :scalar, :__trans-value 16}])})

(fact-skip
"Test demogrify-value map"
(query-engine/demogrify-value  
  {:__trans-type :map, 
   :__trans-value '(
    [{:__trans-type :scalar, :__trans-value 2} {:__trans-type :scalar, :__trans-value 11}] 
    [{:__trans-type :scalar, :__trans-value 4} {:__trans-type :scalar, :__trans-value 16}])})
=> 
  {2 11 4 16})


(fact-skip
"Test transmogrify-value vector"
(query-engine/transmogrify-value
  [1 10 3 15]
  #(+ % 1)
  )
=> 
  {:__trans-type :sequential, 
   :__trans-value '(
    {:__trans-type :scalar, :__trans-value 2} 
    {:__trans-type :scalar, :__trans-value 11} 
    {:__trans-type :scalar, :__trans-value 4} 
    {:__trans-type :scalar, :__trans-value 16})})

(fact-skip
"Test demogrify-value vector"
(query-engine/demogrify-value  
  {:__trans-type :sequential, 
   :__trans-value '(
    {:__trans-type :scalar, :__trans-value 2} 
    {:__trans-type :scalar, :__trans-value 11} 
    {:__trans-type :scalar, :__trans-value 4} 
    {:__trans-type :scalar, :__trans-value 16})}

  )
=> 
  [2 11 4 16])


(fact-skip
"Test transmogrify-value scalar -- returns vector"
(query-engine/transmogrify-value
  2
  #(vector % (+ % 1)))
=> 
  {:__trans-type :scalar, :__trans-value [2 3]})

(fact-skip
"Test demogrify-value scalar -- inputs are vectors"
(query-engine/demogrify-value
  {:__trans-type :scalar, :__trans-value [2 3]})
=> 
  [2 3])


(fact-skip
"Test transmogrify-value map -- returns vector"
(query-engine/transmogrify-value
  {1 10 3 15}
  #(vector % (+ % 1)))
=> 
  {:__trans-type :map, 
   :__trans-value '(
    [{:__trans-type :scalar, :__trans-value [1 2]} {:__trans-type :scalar, :__trans-value [10 11]}] 
    [{:__trans-type :scalar, :__trans-value [3 4]} {:__trans-type :scalar, :__trans-value [15 16]}])})

(fact-skip
"Test demogrify-value map -- inputs are vectors"
(query-engine/demogrify-value
  {:__trans-type :map, 
   :__trans-value '(
    [{:__trans-type :scalar, :__trans-value [1 2]} {:__trans-type :scalar, :__trans-value [10 11]}] 
    [{:__trans-type :scalar, :__trans-value [3 4]} {:__trans-type :scalar, :__trans-value [15 16]}])})
=> 
  {1 10, 2 11, 3 15, 4 16})

(fact-skip
"Test transmogrify-value vector -- returns vector"
(query-engine/transmogrify-value
  [1 10 3 15]
  #(vector % (+ % 1)))
=> 
  {:__trans-type :sequential, 
   :__trans-value '(
    {:__trans-type :scalar, :__trans-value [1 2]} 
    {:__trans-type :scalar, :__trans-value [10 11]} 
    {:__trans-type :scalar, :__trans-value [3 4]} 
    {:__trans-type :scalar, :__trans-value [15 16]})})

(fact-skip
"Test demogrify-value vector -- inputs are vectors"
(query-engine/demogrify-value
  {:__trans-type :sequential, 
   :__trans-value '(
    {:__trans-type :scalar, :__trans-value [1 2]} 
    {:__trans-type :scalar, :__trans-value [10 11]} 
    {:__trans-type :scalar, :__trans-value [3 4]} 
    {:__trans-type :scalar, :__trans-value [15 16]})})
=> 
  [[1 10 3 15] [2 11 4 16]])


(fact-skip
"Test transmogrify-value map -- returns complex"
(query-engine/transmogrify-value
  {1 10 3 15}
  #(vector % (hash-map (+ % 1)(+ % 2))))
=> 
  {:__trans-type :map, :__trans-value '(
    [{:__trans-type :scalar, :__trans-value [1 {2 3}]} {:__trans-type :scalar, :__trans-value [10 {11 12}]}] 
    [{:__trans-type :scalar, :__trans-value [3 {4 5}]} {:__trans-type :scalar, :__trans-value [15 {16 17}]}])})

(fact-skip
"Test transmogrify-value nested -- returns complex"
(query-engine/transmogrify-value
  {1 10 3 [1 22 333]}
  #(vector % (hash-map (+ % 1)(+ % 2))))
=> 
  {:__trans-type :map, 
   :__trans-value '(
    [{:__trans-type :scalar, :__trans-value [1 {2 3}]} {:__trans-type :scalar, :__trans-value [10 {11 12}]}] 
    [{:__trans-type :scalar, :__trans-value [3 {4 5}]} {:__trans-type :sequential, :__trans-value (
      {:__trans-type :scalar, :__trans-value [1 {2 3}]} 
      {:__trans-type :scalar, :__trans-value [22 {23 24}]} 
      {:__trans-type :scalar, :__trans-value [333 {334 335}]})}])})

(fact-skip
"Test transmogrify and demogrify scalar -- with vectorizing"
  (query-engine/mogrify
  2
  #(vector (+ % 1) %))
=> 
  [3 2])

(fact-skip
"Test transmogrify and demogrify nested -- with vectorizing"
(query-engine/demogrify-value 
  (query-engine/transmogrify-value
  {1 10 3 [1 22 333]}
  #(vector (+ % 1) %)))
=> 
  {2 11
   1 10   
   4 [2 23 334]
   3 [1 22 333]})


  (fact-skip
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


(fact-skip
  "Test single-variable expression grammer parser"
  (query-engine/parse-expr "{test.aa}")
  => {:type :var
      :path ["test" "aa"]})

(fact-skip
  "Test single-function expression grammer parser"
  (query-engine/parse-expr "(count {test.aa})")
  => {:type :func-call
      :func "count"
      :args [{:type :var
              :path ["test" "aa"]}]})

(fact-skip
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



(fact-skip
  "Test find-first-and-rest"
  (query-engine/find-first-and-rest even? [1 3 4 1 2 3])
  => [4 [1 2 3]])


  

(fact-skip
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

(fact-skip
  "Test expression grammer parser non-expression"
  (query-engine/parse-expr "test 4")
  => "test 4")


  (fact-skip
    "Test simple eval-expr-var"
    (query-engine/eval-expr-var 
      {:type :var
       :path ["x" "a"]}
      
      [{"_index_" 0, "x" {"a" 1}, "_" [{"a" 1} {"a" 3} {"a" 7}]}, 
       {"_index_" 1, "x" {"a" 3}, "_" [{"a" 1} {"a" 3} {"a" 7}]}, 
       {"_index_" 2, "x" {"a" 7}, "_" [{"a" 1} {"a" 3} {"a" 7}]}])
    
    => `(1 3 7))

  (fact-skip
    "Test simple eval-expr-var-single-context"
    (query-engine/eval-expr-var-single-context
      ["x" "a"]      
      {"_index_" 0, "x" {"a" 1}, "_" [{"a" 1} {"a" 3} {"a" 7}]})
    
    => 1)



  


  (fact-skip
    "Test simple eval expression"
    (query-engine/eval-expr "{obj.prop1}" [{"obj" { "prop0" 10 "prop1" "42"}}])
    => ["42"]
  )

  (fact-skip
    "Test static expression"
    (query-engine/eval-expr "stuff" [{"obj" { "prop0" 10 "prop1" 42}}])
    => "stuff"
  )

(comment

  (fact-skip
    "Test compound eval expression"
    (query-engine/eval-expr "$obj.prop1 is $obj.prop0.abc" [{"obj" { "prop0" {"abc" "ok"} "prop1" "42"}}])
    => "42 is ok"
  )

)


  (fact-skip
  "Test get-for-contexts"
  (query-engine/get-for-contexts {"for" "x" "in" "_"} {"_" [{:a 1} {:a 3} {:a 7}]})
=> [
    { "x" {:a 1}, "_" [{:a 1} {:a 3} {:a 7}]}
    { "x" {:a 3}, "_" [{:a 1} {:a 3} {:a 7}]}
    { "x" {:a 7}, "_" [{:a 1} {:a 3} {:a 7}]}    
  ])



(fact-skip
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

  (fact-skip
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

  (fact-skip
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
 
  (fact-skip
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
 
 

(fact-skip
    "***test-squery -- Test confusing idea #1"
    (query-engine/test-squery
    { "select"  {"{x.prop1}" "{x.prop2}"}
      "for"   "x"
      "in"    "_"
      "group" "1"}

    [{"prop1" "k11", "prop2" "v222"}
     {"prop1" "k33", "prop2" "v444"}])

  => [{"k11" "v222"
       "k33" "v444"}])

  (fact-skip
    "test-squery -- Test confusing idea #2"
    (query-engine/test-squery
    { "select"  {"aa" "(merge {x.prop1} {x.prop2})"}
      "for"   "x"
      "in"    "_"
      "group" "1" }
      
    [{"prop1" "11", "prop2" "222"}
     {"prop1" "33", "prop2" "444"}])

  => [{ "aa" ["11" "33" "222" "444"]}])


  (fact-skip
    "test-squery -- Test confusing ideas together"
    (query-engine/test-squery
    { "select"  {"array"      "(merge {x.prop1} {x.prop2})"
                 "keys"       {"{x.prop1}" "{x.prop2}"}
                 "aggregate"  "(sum {x.prop1})"}
      "for"   "x"
      "in"    "_"
      "group" "1"}

    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}]

    )
  => [{ "array"     [11 33 222 444] 
        "keys"      {11          222
                     33          444}
        "aggregate" 44}])

  
  (fact-skip
  "test-squery -- Test selects single property from object"
  (query-engine/test-squery
    {
      "select"  "{x.prop1}"
      "for"   "x"
      "in"    "_"
    }

    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[11] [33]])

  
  (fact-skip
  "test-squery -- Test selects single property from object - aggregated group w/o function?"
  (query-engine/test-squery
    {
      "select"  "{x.prop1}"
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[11 33]])

  (fact-skip
  "test-squery -- Test selects and merges single property from object - grouped"
  (query-engine/test-squery
    {
      "select"  "(merge {x.prop1})"
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[11 33]])

  (fact-skip
  "test-squery -- Test selects single property as arrays from object - grouped"
  (query-engine/test-squery
    {
      "select"  ["{x.prop1}"]
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[[11] [33]]])



  (fact-skip
  "test-squery -- Test selects and merges two properties from object - grouped"
  (query-engine/test-squery
    {
      "select"  "(merge {x.prop1} {x.prop2})"
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[11 33 222 444]])



  (fact-skip
  "test-squery -- Test selects two properties as arrays from object - grouped"
  (query-engine/test-squery
    {
      "select"  ["{x.prop1}" "{x.prop2}"]
      "for"   "x"
      "in"    "_"
      "group" 1
    }
    [{"prop1" 11, "prop2" 222}
     {"prop1" 33, "prop2" 444}])
  => [[[11 222] [33 444]]])




  (fact-skip
  "test-squery -- Test selects multiple properties from nested objects in array"
  (query-engine/test-squery
    { "select"  { "key" "{x.prop1}", "{x.prop2.key1}" "{x.prop2.prop3}" }
      "for"   "x"
      "in"    "_"}

    [{"prop1" "11", "prop2" {"key1" "22" "prop3" "33"}}
     {"prop1" "AA", "prop2" {"key1" "BB" "prop3" "CC"}}
     {"prop1" "22", "prop2" {"key1" "44" "prop3" "66"}}])

  => [{"key" "11", "22" "33"}
      {"key" "AA", "BB" "CC"}
      {"key" "22", "44" "66"}])


  
  (fact-skip
  "Test count aggregate function -- simple single group"
  (query-engine/test-squery
    { "select"  "(count {x})"
      "for"   "x"
      "in"    "_"
      "group" "1"
    }

    ["2", "3", "4", "5"]

    )
  => [4])

  (fact-skip
  "Test count aggregate function"
  (query-engine/test-squery
    { "select"  "(a-count {x.a})"
      "for"   "x"
      "in"    "_"      
    }

    [{"a" [1 3 5]}
     {"a" [2 2 4 4 12]}]

    )
  => [3 5])

  
  (fact-skip
  "Test sum aggregate function -- simple single group"
  (query-engine/test-squery
    { "select"  "(sum {x})"
      "for"   "x"
      "in"    "_"
      "group" "1"
    }

    [2, 3, 4, 5]

    )
  => [14])

  (fact-skip
  "Test sum aggregate function"
  (query-engine/test-squery
    { "select"  "(a-sum {x.a})"
      "for"   "x"
      "in"    "_"      
    }

    [{"a" [1 3 5]}
     {"a" [2 2 4 4 12]}]

    )
  => [9 24])


  (fact-skip
  "Test merge no group"
  (query-engine/test-squery
    { "select" {"sum"   "(sum (merge {x.a}))"
                "count" "(count (merge {x.a}))"}
      "for"   "x"
      "in"    "_"      
    }

    [{"a" [1 3 5]}
     {"a" [2 2 4 4 12]}]

    )
  => [{"sum" 9, "count" 3} {"sum" 24, "count" 5}])

  (fact-skip
  "Test merge group"
  (query-engine/test-squery
    { "select" {"sum"   "(sum (merge {x.a}))"
                "count" "(count (merge {x.a}))"}
      "for"   "x"
      "in"    "_"
      "group" "1"
    }

    [{"a" [1 3 5]}
     {"a" [2 2 4 4 12]}]

    )
  => [{"sum" 33, "count" 8}])


  (fact-skip
  "Test merge group by variable"
  (query-engine/test-squery
    { "select" {"sum"   "(sum (merge {x.a}))"
                "count" "(count (merge {x.a}))"
                "distinct" "(count (distinct (merge {x.a})))"
                "avg" "(avg (merge {x.a}))"
                "max" "(max (merge {x.a}))"
                "min" "(min (merge {x.a}))"
                "first" "(first (merge {x.a}))"
                "last" "(last (merge {x.a}))"

              }
      "for"   "x"
      "in"    "_"
      "group" "{x.b}"
    }

    [{"a" [1 3 5],      "b" 1}
     {"a" [2 2 4 4 12], "b" 1}
     {"a" [3],          "b" 2}
     {"a" [2 10],       "b" 2}])

  => [{"sum" 33, "count" 8, "distinct" 6, "avg" 4.125, "max" 12, "min" 1, "first" 1, "last" 12}
      {"sum" 15, "count" 3, "distinct" 3, "avg" 5.0, "max" 10, "min" 2, "first" 3, "last" 10}])


  (fact-skip
  "Test vector group index"
  (query-engine/test-squery
    { "select" {"sum"   "(sum (merge {x.a}))"
                "count" "(count (merge {x.a}))"
                "b"     "(first {x.b})"
              }
      "for"   "x"
      "in"    "_"
      "group" ["{x.b}" "{x.c}"]
    }

    [{"a" [1 2],  "b" 1 "c" 1}
     {"a" [3],    "b" 1 "c" 1}
     {"a" [4 5],  "b" 2 "c" 1}
     {"a" [6],    "b" 2 "c" 2}])

  => [{"sum" 6, "count" 3, "b" 1}
      {"sum" 9, "count" 2, "b" 2}
      {"sum" 6, "count" 1, "b" 2}])

  (fact-skip
  "Test JSON query"
  (query-engine/test-query
    "{ \"select\": {\"sum\" : \"(sum (merge {x.a}))\",
                  \"count\" : \"(count (merge {x.a}))\",
                  \"b\":     \"(first {x.b})\"
              },
      \"for\" : \"x\",
      \"in\" :  \"_\",
      \"group\"  : [\"{x.b}\", \"{x.c}\"]
    }"

    "[{\"a\": [1, 2],  \"b\": 1, \"c\": 1},
      {\"a\": [3],    \"b\": 1, \"c\": 1},
      {\"a\": [4, 5],  \"b\": 2, \"c\": 1},
      {\"a\": [6],    \"b\": 2, \"c\": 2}]")

  =>  "[{\"b\":1,\"sum\":6,\"count\":3},{\"b\":2,\"sum\":9,\"count\":2},{\"b\":2,\"sum\":6,\"count\":1}]")




(comment

(fact-skip
  "Test selects simple property correctly"
  (query-engine/test-squery
    {"select" "{prop1}"}
    {"prop1" 22, "prop2" 34})
  => 22)

)
